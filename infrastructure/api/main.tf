data "aws_subnet" "private" {
  count = length(var.private_subnet_ids)
  id    = var.private_subnet_ids[count.index]
}

# Used to get AWS account number without putting it in the repo
data "aws_caller_identity" "current" {}
data "aws_region" "current" {}

# ALB Security group

resource "aws_security_group" "api-loadbalancer" {
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
}

# Load balancer to service

resource "aws_alb" "main" {
  name            = "foreign-language-reader-${var.env}"
  subnets         = var.public_subnet_ids
  security_groups = [aws_security_group.api-loadbalancer.id]
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

resource "aws_ecr_repository" "foreign-language-reader-api" {
  name                 = "foreign-language-reader"
  image_tag_mutability = "IMMUTABLE"

  image_scanning_configuration {
    scan_on_push = true
  }
}

# Database

resource "aws_db_subnet_group" "main" {
  name       = "foreign-language-reader-${var.env}"
  subnet_ids = var.private_subnet_ids
}

resource "aws_security_group" "database" {
  name        = "foreign-language-reader-database-${var.env}"
  description = "Database security group for foreign language reader ${var.env}. Only allows connections from inside the subnet."
  vpc_id      = var.vpc_id

  ingress {
    from_port   = 3306
    to_port     = 3306
    protocol    = "tcp"
    cidr_blocks = var.private_subnet_cidrs
  }

  tags = {
    Name = "database-api-foreign-language-reader"
  }
}

resource "aws_db_instance" "default" {
  allocated_storage      = 20
  max_allocated_storage  = 1000
  storage_type           = "gp2"
  engine                 = "mysql"
  engine_version         = "5.7"
  instance_class         = "db.${var.instance_size}"
  identifier             = "foreign-language-reader-${var.env}"
  username               = var.rds_username
  password               = var.rds_password
  parameter_group_name   = "default.mysql5.7"
  skip_final_snapshot    = true
  vpc_security_group_ids = [aws_security_group.database.id]
  db_subnet_group_name   = aws_db_subnet_group.main.id
}

# The fargate cluster

resource "aws_ecs_cluster" "main" {
  name = "foreign-language-reader-${var.env}"
}

# The task role

data "aws_iam_policy_document" "task-assume-role-policy" {
  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["ecs-tasks.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "task_exec" {
  name               = "foreign-language-reader-api-${var.env}"
  assume_role_policy = data.aws_iam_policy_document.task-assume-role-policy.json
}

resource "aws_iam_role_policy_attachment" "allow_logging" {
  role       = aws_iam_role.task_exec.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

# The task

data "template_file" "api_task" {
  template = file("${path.module}/container_definition.json")

  vars = {
    image           = aws_ecr_repository.foreign-language-reader-api.repository_url
    secret_key_base = var.secret_key_base
    database_url    = "ecto://${var.rds_username}:${var.rds_password}@${aws_db_instance.default.endpoint}/foreign-language-reader"
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
  execution_role_arn       = aws_iam_role.task_exec.arn
  task_role_arn            = aws_iam_role.task_exec.arn
}

resource "aws_security_group" "ecs_tasks" {
  name        = "foreign-language-reader-api-tasks-${var.env}"
  description = "Only permits access from the load balancer"
  vpc_id      = var.vpc_id

  ingress {
    protocol        = "tcp"
    from_port       = "4000"
    to_port         = "4000"
    security_groups = [aws_security_group.api-loadbalancer.id]
  }

  egress {
    protocol    = "-1"
    from_port   = 0
    to_port     = 0
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_ecs_service" "api" {
  name            = "foreign-language-reader-api-${var.env}"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.api.arn
  desired_count   = 1 # TODO handle scaling
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
}

# Codebuild role

data "aws_iam_policy_document" "codebuild_policy" {
  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["codebuild.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "codebuild_role" {
  name               = "foreign-language-reader-api-build-${var.env}"
  assume_role_policy = data.aws_iam_policy_document.codebuild_policy.json
}

data "aws_iam_policy_document" "build_in_vpc" {
  statement {
    actions   = ["ec2:CreateNetworkInterface", "ec2:DescribeDhcpOptions", "ec2:DescribeNetworkInterfaces", "ec2:DeleteNetworkInterface", "ec2:DescribeSubnets", "ec2:DescribeSecurityGroups", "ec2:DescribeVpcs"]
    effect    = "Allow"
    resources = ["*"]
  }
  statement {
    actions   = ["ec2:CreateNetworkInterfacePermission"]
    effect    = "Allow"
    resources = ["arn:aws:ec2:${data.aws_region.current.name}:${data.aws_caller_identity.current.account_id}:network-interface/*"]
    condition {
      test     = "StringEquals"
      variable = "ec2:Subnet"

      values = data.aws_subnet.private.*.arn
    }
  }
}

resource "aws_iam_policy" "codebuild_permissions" {
  description = "IAM policy for building foreign-language-reader api in codebuild."

  policy = data.aws_iam_policy_document.build_in_vpc.json
}

resource "aws_iam_role_policy_attachment" "codebuild_permissions" {
  role       = aws_iam_role.codebuild_role.name
  policy_arn = aws_iam_policy.codebuild_permissions.arn
}

# Codebuild job

resource "aws_codebuild_source_credential" "github" {
  auth_type   = "PERSONAL_ACCESS_TOKEN"
  server_type = "GITHUB"
  token       = var.github_token
}

resource "aws_s3_bucket" "foreign_language_reader_api_build" {
  bucket = "foreign-language-reader-api-build"
  acl    = "private"
}

resource "aws_security_group" "codebuild" {
  name        = "foreign-language-reader-codebuild"
  description = "Codebuild worker security group"
  vpc_id      = var.vpc_id

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "build-api-foreign-language-reader"
  }
}

resource "aws_codebuild_project" "api_build" {
  name          = "foreign-language-reader-api"
  description   = "The build job for the foreign language reader"
  build_timeout = "15"
  service_role  = aws_iam_role.codebuild_role.arn

  artifacts {
    type = "NO_ARTIFACTS"
  }

  cache {
    type     = "S3"
    location = aws_s3_bucket.foreign_language_reader_api_build.bucket
  }

  environment {
    compute_type                = "BUILD_GENERAL1_SMALL"
    image                       = "aws/codebuild/standard:1.0"
    type                        = "LINUX_CONTAINER"
    image_pull_credentials_type = "CODEBUILD"
  }

  logs_config {
    s3_logs {
      status   = "ENABLED"
      location = "${aws_s3_bucket.foreign_language_reader_api_build.id}/build-log"
    }
  }

  source {
    type            = "GITHUB"
    location        = "https://github.com/lucaskjaero/foreign-language-reader.git"
    git_clone_depth = 1
  }

  vpc_config {
    vpc_id = var.vpc_id

    security_group_ids = [aws_security_group.codebuild.id]
  }

  tags = {
    Environment = "Build"
  }
}

resource "aws_codebuild_webhook" "github_webhook" {
  project_name = aws_codebuild_project.api_build.name

  filter_group {
    filter {
      type    = "EVENT"
      pattern = "PUSH"
    }

    filter {
      type    = "HEAD_REF"
      pattern = "master"
    }
  }
}
