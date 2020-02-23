terraform {
  backend "remote" {
    hostname     = "app.terraform.io"
    organization = "foreign-language-reader"

    workspaces {
      name = "foreign-language-reader"
    }
  }
}

variable "digitalocean_token" {}
variable "test_environment" {
  default = false
}

provider "aws" {
  profile = "default"
  region  = "us-west-2"
}

provider "digitalocean" {
  token = var.do_token
}

// Hosts the container registries
module "aws" {
  source = "./infrastructure/terraform/aws"
}

# Hosts everything else
module "digitalocean" {
  source           = "./infrastructure/terraform/digitalocean"
  test_environment = var.test_environment
}
