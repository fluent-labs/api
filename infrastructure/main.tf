resource "aws_vpc" "main" {
  cidr_block = var.cidr_block
}

resource "aws_network_acl" "main" {
  vpc_id = aws_vpc.main.id
}

resource "aws_network_acl_rule" "block_all_inbound_unless_allowed" {
  network_acl_id = aws_network_acl.main.id
  rule_number    = 100
  egress         = false
  protocol       = "all"
  cidr_block     = "0.0.0.0/0"
  rule_action    = "deny"
}

resource "aws_network_acl_rule" "allow_all_outbound" {
  network_acl_id = aws_network_acl.main.id
  rule_number    = 101
  egress         = false
  protocol       = "all"
  cidr_block     = "0.0.0.0/0"
  rule_action    = "allow"
}

resource "aws_network_acl_rule" "inbound_http_traffic" {
  network_acl_id = aws_network_acl.main.id
  rule_number    = 200
  egress         = false
  protocol       = "tcp"
  cidr_block     = "0.0.0.0/0"
  rule_action    = "allow"
  from_port      = 80
  to_port        = 80
}

resource "aws_network_acl_rule" "inbound_tls_traffic" {
  network_acl_id = aws_network_acl.main.id
  rule_number    = 201
  egress         = false
  protocol       = "tcp"
  cidr_block     = "0.0.0.0/0"
  rule_action    = "allow"
  from_port      = 443
  to_port        = 443
}

resource "aws_network_acl_rule" "inbound_ssh_traffic" {
  network_acl_id = aws_network_acl.main.id
  rule_number    = 202
  egress         = false
  protocol       = "tcp"
  cidr_block     = "0.0.0.0/0"
  rule_action    = "allow"
  from_port      = 22
  to_port        = 22
}

resource "aws_subnet" "main" {
  for_each = toset(var.subnet_cidr_blocks)

  vpc_id     = aws_vpc.main.id
  cidr_block = each.value
}

module "api" {
  source        = "./api"
  env           = var.env
  instance_size = var.instance_size
}

module "frontend" {
  source = "./frontend"
  env    = var.env
}

module "vocabulary-lambda" {
  source = "./vocabulary"
  env    = var.env
}
