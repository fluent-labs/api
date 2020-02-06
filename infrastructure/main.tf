resource "aws_vpc" "main" {
  cidr_block = var.cidr_block

  tags = {
    Name = "Foreign-Language-Reader-${var.env}"
  }
}

data "aws_availability_zones" "available" {}

resource "aws_subnet" "private" {
  count             = 2
  cidr_block        = cidrsubnet(aws_vpc.main.cidr_block, 4, count.index)
  availability_zone = data.aws_availability_zones.available.names[count.index]
  vpc_id            = aws_vpc.main.id

  tags = {
    Name = "API-Private-Subnet-${count.index}"
  }
}

resource "aws_subnet" "public" {
  count                   = 2
  cidr_block              = cidrsubnet(aws_vpc.main.cidr_block, 4, 2 + count.index)
  availability_zone       = data.aws_availability_zones.available.names[count.index]
  vpc_id                  = aws_vpc.main.id
  map_public_ip_on_launch = true

  tags = {
    Name = "API-Public-Subnet-${count.index}"
  }
}

resource "aws_network_acl" "main" {
  vpc_id     = aws_vpc.main.id
  subnet_ids = [aws_subnet.public[0].id, aws_subnet.public[1].id]
}

resource "aws_network_acl_rule" "block_all_inbound_unless_allowed" {
  network_acl_id = aws_network_acl.main.id
  rule_number    = 101
  egress         = false
  protocol       = "all"
  cidr_block     = "0.0.0.0/0"
  rule_action    = "deny"
}

resource "aws_network_acl_rule" "allow_all_outbound" {
  network_acl_id = aws_network_acl.main.id
  rule_number    = 102
  egress         = true
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

# Route the public subnet traffic through the IGW
resource "aws_route" "internet_access" {
  route_table_id         = aws_vpc.main.main_route_table_id
  destination_cidr_block = "0.0.0.0/0"
  gateway_id             = aws_internet_gateway.gw.id
}

resource "aws_internet_gateway" "gw" {
  vpc_id = aws_vpc.main.id
}

resource "aws_eip" "gw" {
  count      = 2
  vpc        = true
  depends_on = [aws_internet_gateway.gw]

  tags = {
    Name = "API-Public-Subnet-${count.index}"
  }
}

resource "aws_nat_gateway" "gw" {
  count         = 2
  subnet_id     = element(aws_subnet.public.*.id, count.index)
  allocation_id = element(aws_eip.gw.*.id, count.index)

  tags = {
    Name = "API-Public-Subnet-${count.index}"
  }
}

# Create a new route table for the private subnets
# And make it route non-local traffic through the NAT gateway to the internet
resource "aws_route_table" "private" {
  count  = 2
  vpc_id = aws_vpc.main.id

  route {
    cidr_block     = "0.0.0.0/0"
    nat_gateway_id = element(aws_nat_gateway.gw.*.id, count.index)
  }

  tags = {
    Name = "API-Private-Subnet-${count.index}"
  }
}

# Explicitly associate the newly created route tables to the private subnets (so they don't default to the main route table)
resource "aws_route_table_association" "private" {
  count          = 2
  subnet_id      = element(aws_subnet.private.*.id, count.index)
  route_table_id = element(aws_route_table.private.*.id, count.index)
}

module "api" {
  source             = "./api"
  env                = var.env
  instance_size      = var.instance_size
  vpc_id             = aws_vpc.main.id
  private_subnet_ids = aws_subnet.private.*.id
  public_subnet_ids  = aws_subnet.public.*.id
  rds_username       = var.rds_username
  rds_password       = var.rds_password
}

module "frontend" {
  source = "./frontend"
  env    = var.env
}

module "vocabulary-lambda" {
  source = "./vocabulary"
  env    = var.env
}
