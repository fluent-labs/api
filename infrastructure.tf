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

variable "secret_key_base" {
  default = "KN4rxjPOfxRk3uo7fD928e6nt12jzrvy5t90fp7Snlp63ckc0rRZTglirGt+WiB6"
}

module "dev" {
  source          = "./infrastructure"
  env             = "dev"
  instance_size   = "t2.micro"
  cpu             = "256"
  memory          = "512"
  cidr_block      = "172.32.0.0/16"
  rds_username    = var.rds_username
  rds_password    = var.rds_password
  secret_key_base = var.secret_key_base
}
