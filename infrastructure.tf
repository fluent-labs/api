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

module "frontend" {
  source = "./frontend"
}

module "dev" {
  source = "./infrastructure"
  env = "dev"
}
