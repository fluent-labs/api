data "aws_subnet" "private" {
  count = length(var.private_subnet_ids)
  id    = var.private_subnet_ids[count.index]
}

resource "aws_db_subnet_group" "main" {
  name       = "foreign-language-reader"
  subnet_ids = var.private_subnet_ids
}

resource "aws_security_group" "database" {
  name        = "foreign-language-reader-database"
  description = "Database security group for foreign language reader. Only allows connections from inside the subnet."
  vpc_id      = var.vpc_id

  ingress {
    from_port   = 3306
    to_port     = 3306
    protocol    = "tcp"
    cidr_blocks = data.aws_subnet.private.*.cidr_block
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
  identifier             = "foreign-language-reader"
  username               = var.rds_username
  password               = var.rds_password
  parameter_group_name   = "default.mysql5.7"
  skip_final_snapshot    = true
  vpc_security_group_ids = [aws_security_group.database.id]
  db_subnet_group_name   = aws_db_subnet_group.main.id
}
