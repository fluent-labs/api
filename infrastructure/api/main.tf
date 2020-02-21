data "aws_subnet" "private" {
  count = length(var.private_subnet_ids)
  id    = var.private_subnet_ids[count.index]
}

data "aws_subnet" "public" {
  count = length(var.public_subnet_ids)
  id    = var.public_subnet_ids[count.index]
}

data "aws_ecs_cluster" "main" {
  cluster_name = var.cluster_name
}

# ALB Security group

resource "aws_security_group" "api_loadbalancer" {
  name        = "foreign-language-reader-api-loadbalancer-${var.env}"
  description = "Allows access to the api"
  vpc_id      = var.vpc_id

  # TODO serve TLS when I have a domain name
  ingress {
    protocol    = "tcp"
    from_port   = 80
    to_port     = 80
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "loadbalancer-api-foreign-language-reader"
  }
}

# Load balancer to service

resource "aws_alb" "main" {
  name            = "foreign-language-reader-${var.env}"
  subnets         = var.public_subnet_ids
  security_groups = [aws_security_group.api_loadbalancer.id]
}

resource "aws_alb_target_group" "app" {
  name        = "foreign-language-reader-api-${var.env}"
  port        = 4000
  protocol    = "HTTP"
  vpc_id      = var.vpc_id
  target_type = "ip"

  health_check {
    matcher = "200-299"
    path    = "/health"
  }
}

resource "aws_alb_listener" "front_end" {
  load_balancer_arn = aws_alb.main.id
  port              = "80"
  protocol          = "HTTP"

  default_action {
    target_group_arn = aws_alb_target_group.app.id
    type             = "forward"
  }
}

resource "aws_ecr_repository" "foreign_language_reader_api" {
  name                 = "foreign-language-reader-api"
  image_tag_mutability = "MUTABLE"

  image_scanning_configuration {
    scan_on_push = true
  }
}

resource "aws_ecr_lifecycle_policy" "clean_up_container_registry" {
  repository = aws_ecr_repository.foreign_language_reader_api.name

  policy = <<EOF
{
  "rules": [
    {
      "action": {
        "type": "expire"
      },
      "selection": {
        "countType": "imageCountMoreThan",
        "countNumber": 5,
        "tagStatus": "any"
      },
      "description": "Only keep the last 5 images",
      "rulePriority": 1
    }
  ]
}
EOF
}

# The task

data "template_file" "api_task" {
  template = file("${path.module}/container_definition.json")

  vars = {
    image           = "${aws_ecr_repository.foreign_language_reader_api.repository_url}:latest"
    secret_key_base = var.secret_key_base
    database_url    = "ecto://${var.rds_username}:${var.rds_password}@${var.database_endpoint}/foreign-language-reader-${var.env}"
    log_group       = "foreign-language-reader-api-${var.env}"
    env             = var.env
  }
}

resource "aws_ecs_task_definition" "api" {
  family                   = "foreign-language-reader-api-${var.env}"
  container_definitions    = data.template_file.api_task.rendered
  requires_compatibilities = ["FARGATE"]
  network_mode             = "awsvpc"
  cpu                      = var.cpu
  memory                   = var.memory
  execution_role_arn       = var.iam_role
  task_role_arn            = var.iam_role
}

resource "aws_security_group" "ecs_tasks" {
  name        = "foreign-language-reader-api-tasks-${var.env}"
  description = "Only permits access from the load balancer"
  vpc_id      = var.vpc_id

  ingress {
    protocol        = "tcp"
    from_port       = "4000"
    to_port         = "4000"
    security_groups = [aws_security_group.api_loadbalancer.id]
  }

  egress {
    protocol    = "-1"
    from_port   = 0
    to_port     = 0
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "task-api-foreign-language-reader"
  }
}

resource "aws_ecs_service" "api" {
  name            = "foreign-language-reader-api-${var.env}"
  cluster         = data.aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.api.arn
  desired_count   = var.default_capacity
  launch_type     = "FARGATE"

  network_configuration {
    security_groups = [aws_security_group.ecs_tasks.id]
    subnets         = data.aws_subnet.private.*.id
  }

  load_balancer {
    target_group_arn = aws_alb_target_group.app.id
    container_name   = aws_ecs_task_definition.api.family
    container_port   = 4000
  }

  depends_on = [
    aws_alb_listener.front_end
  ]
  lifecycle {
    ignore_changes = [desired_count, task_definition]
  }
}

resource "aws_cloudwatch_log_group" "foreign_language_reader_api" {
  name              = aws_ecs_service.api.name
  retention_in_days = 90

  tags = {
    Name = aws_ecs_service.api.name
  }
}

resource "aws_appautoscaling_target" "target" {
  service_namespace  = "ecs"
  resource_id        = "service/${var.cluster_name}/${aws_ecs_service.api.name}"
  scalable_dimension = "ecs:service:DesiredCount"
  role_arn           = var.fargate_autoscale_role
  min_capacity       = var.min_capacity
  max_capacity       = var.max_capacity
}

resource "aws_appautoscaling_policy" "up" {
  name               = "${var.env}_scale_up"
  service_namespace  = "ecs"
  resource_id        = "service/${var.cluster_name}/${aws_ecs_service.api.name}"
  scalable_dimension = "ecs:service:DesiredCount"


  step_scaling_policy_configuration {
    adjustment_type         = "ChangeInCapacity"
    cooldown                = 60
    metric_aggregation_type = "Maximum"

    step_adjustment {
      metric_interval_lower_bound = 0
      scaling_adjustment          = 1
    }
  }

  depends_on = [aws_appautoscaling_target.target]
}

resource "aws_appautoscaling_policy" "down" {
  name               = "${var.env}_scale_down"
  service_namespace  = "ecs"
  resource_id        = "service/${var.cluster_name}/${aws_ecs_service.api.name}"
  scalable_dimension = "ecs:service:DesiredCount"

  step_scaling_policy_configuration {
    adjustment_type         = "ChangeInCapacity"
    cooldown                = 60
    metric_aggregation_type = "Maximum"

    step_adjustment {
      metric_interval_lower_bound = 0
      scaling_adjustment          = -1
    }
  }

  depends_on = [aws_appautoscaling_target.target]
}

resource "aws_cloudwatch_metric_alarm" "service_cpu_high" {
  alarm_name          = "${var.env}_openjobs_api_cpu_utilization_high"
  comparison_operator = "GreaterThanOrEqualToThreshold"
  evaluation_periods  = "2"
  metric_name         = "CPUUtilization"
  namespace           = "AWS/ECS"
  period              = "60"
  statistic           = "Maximum"
  threshold           = "85"

  dimensions = {
    ClusterName = var.cluster_name
    ServiceName = aws_ecs_service.api.name
  }

  alarm_actions = [aws_appautoscaling_policy.up.arn]
  ok_actions    = [aws_appautoscaling_policy.down.arn]
}
