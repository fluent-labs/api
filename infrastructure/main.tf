resource "aws_vpc" "main" {
  cidr_block = var.cidr_block
}

resource "aws_network_acl" "main" {
  vpc_id = aws_vpc.main.id
}

module "api" {
  source = "./api"
}

module "frontend" {
  source = "./frontend"
}

module "vocabulary-lambda" {
  source = "./vocabulary"
  env    = var.env
}
