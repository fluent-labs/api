data "aws_subnet" "private" {
  count = length(var.private_subnet_ids)
  id    = var.private_subnet_ids[count.index]
}

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
    path = "/health"
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
  deletion_protection    = true
  vpc_security_group_ids = [aws_security_group.database.id]
  db_subnet_group_name   = aws_db_subnet_group.main.id
}
