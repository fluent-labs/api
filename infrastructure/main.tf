module "network" {
  source     = "./network"
  cidr_block = var.cidr_block
}

# module "database" {
#
# }

module "api" {
  source               = "./api"
  env                  = var.env
  instance_size        = var.instance_size
  cpu                  = var.cpu
  memory               = var.memory
  vpc_id               = module.network.vpc_id
  private_subnet_ids   = module.network.private_subnet_ids
  public_subnet_ids    = module.network.public_subnet_ids
  rds_username         = var.rds_username
  rds_password         = var.rds_password
  secret_key_base      = var.secret_key_base
  github_token         = var.github_token
}

module "frontend" {
  source = "./frontend"
  env    = var.env
}

module "vocabulary-lambda" {
  source = "./vocabulary"
  env    = var.env
}
