locals {
  vocabulary_deploy_bucket = "vocabulary-lambda-deploy"
}

module "network" {
  source     = "./network"
  cidr_block = var.cidr_block
}

module "roles" {
  source                   = "./roles"
  private_subnet_ids       = module.network.private_subnet_ids
  vocabulary_deploy_bucket = local.vocabulary_deploy_bucket
}

module "database" {
  source             = "./database"
  vpc_id             = module.network.vpc_id
  private_subnet_ids = module.network.private_subnet_ids
  instance_size      = var.instance_size
  rds_username       = var.rds_username
  rds_password       = var.rds_password
}

module "api" {
  source             = "./api"
  env                = var.env
  api_role           = module.roles.api_role
  cpu                = var.cpu
  memory             = var.memory
  vpc_id             = module.network.vpc_id
  private_subnet_ids = module.network.private_subnet_ids
  public_subnet_ids  = module.network.public_subnet_ids
  database_endpoint  = module.database.database_endpoint
  rds_username       = var.rds_username
  rds_password       = var.rds_password
  secret_key_base    = var.secret_key_base
}

module "frontend_prod" {
  source = "./frontend"
  env    = var.env
}

module "frontend_dev" {
  source = "./frontend"
  env    = "dev"
}

module "frontend_storybook" {
  source = "./frontend"
  env    = "storybook"
}

module "vocabulary-lambda" {
  source                   = "./vocabulary"
  env                      = var.env
  vocabulary_deploy_bucket = local.vocabulary_deploy_bucket
  vocabulary_role          = module.roles.vocabulary_role
}

# The CI/CD configuration for this application
module "pipeline" {
  source             = "./pipeline"
  codebuild_role     = module.roles.codebuild_role
  codepipeline_role  = module.roles.codepipeline_role
  vpc_id             = module.network.vpc_id
  private_subnet_ids = module.network.private_subnet_ids
  github_token       = var.github_token
  api_ecr_name       = module.api.ecr_name
  api_cluster_name   = module.api.cluster_name
  api_service_name   = module.api.service_name
}
