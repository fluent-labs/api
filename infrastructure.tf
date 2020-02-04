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

module "dev" {
  source             = "./infrastructure"
  env                = "dev"
  instance_size      = "t2.micro"
  cidr_block         = "10.0.0.0/16"
  subnet_cidr_blocks = ["10.0.1.0/24"]
}
