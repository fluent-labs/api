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

provider "kubernetes" {
  load_config_file = false
  host             = digitalocean_kubernetes_cluster.foreign_language_reader.endpoint
  token            = digitalocean_kubernetes_cluster.foreign_language_reader.kube_config[0].token
  cluster_ca_certificate = base64decode(
    digitalocean_kubernetes_cluster.foreign_language_reader.kube_config[0].cluster_ca_certificate
  )
}

// Hosts the container registries
module "aws" {
  source = "./infrastructure/terraform/aws"
}

# Hosts everything else
module "digitalocean" {
  source           = "./infrastructure/terraform/digitalocean"
  cluster_name     = digitalocean_kubernetes_cluster.foreign_language_reader.name
  test_environment = var.test_environment
}
