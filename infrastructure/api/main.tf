data "aws_subnet" "one" {
  id = "${var.subnet_id_one}"
}

data "aws_subnet" "two" {
  id = "${var.subnet_id_two}"
}

resource "aws_db_subnet_group" "main" {
  name       = "foreign-language-reader-${var.env}"
  subnet_ids = [data.aws_subnet.one.id, data.aws_subnet.two.id]
}

resource "aws_security_group" "database" {
  name        = "foreign-language-reader-database-${var.env}"
  description = "Database security group for foreign language reader ${var.env}. Only allows connections from inside the subnet."
  vpc_id      = data.aws_subnet.main.vpc_id

  ingress {
    from_port   = 3306
    to_port     = 3306
    protocol    = "tcp"
    cidr_blocks = [data.aws_subnet.main.cidr_block]
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
