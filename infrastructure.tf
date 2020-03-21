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
  token = var.digitalocean_token
}

provider "acme" {
  server_url = "https://acme-v02.api.letsencrypt.org/directory"
}

# Held here so that Helm and K8s providers can be initialized to work on this cluster
resource "digitalocean_kubernetes_cluster" "foreign_language_reader" {
  name    = "foreign-language-reader"
  region  = "sfo2"
  version = "1.16.6-do.0"
  tags    = ["prod"]

  node_pool {
    name       = "worker-pool"
    size       = "s-2vcpu-4gb"
    auto_scale = true
    min_nodes  = 1
    max_nodes  = 5
  }
}

module "infrastructure" {
  source             = "./infrastructure/terraform"
  cluster_name       = digitalocean_kubernetes_cluster.foreign_language_reader.name
  test_environment   = var.test_environment
  digitalocean_token = var.digitalocean_token
}
