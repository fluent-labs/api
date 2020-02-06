terraform {
  backend "remote" {
    hostname     = "app.terraform.io"
    organization = "foreign-language-reader"

    workspaces {
      name = "foreign-language-reader"
    }
  }
}

provider "aws" {
  profile = "default"
  region  = "us-west-2"
}

variable "rds_username" {
  default = "test_username"
}

variable "rds_password" {
  default = "test_username"
}

module "dev" {
  source        = "./infrastructure"
  env           = "dev"
  instance_size = "t2.micro"
  cidr_block    = "172.32.0.0/16"
  rds_username  = var.rds_username
  rds_password  = var.rds_password
}
