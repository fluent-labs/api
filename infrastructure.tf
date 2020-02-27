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
variable "api_secret_key_base" {}

provider "aws" {
  profile = "default"
  region  = "us-west-2"
}

provider "digitalocean" {
  token = var.digitalocean_token
}

resource "digitalocean_kubernetes_cluster" "foreign_language_reader" {
  name    = "foreign-language-reader"
  region  = "sfo2"
  version = "1.16.6-do.0"
  tags    = ["prod"]

  node_pool {
    name       = "worker-pool"
    size       = "s-1vcpu-2gb"
    auto_scale = true
    min_nodes  = 1
    max_nodes  = 2
  }
}

module "infrastructure" {
  source              = "./infrastructure/terraform"
  cluster_name        = digitalocean_kubernetes_cluster.foreign_language_reader.name
  api_secret_key_base = var.api_secret_key_base
  test_environment    = var.test_environment
}
