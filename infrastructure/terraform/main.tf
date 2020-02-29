# Hold K8s configuration in an intermediate level
# Terraform currently cannot create a cluster and use it to set up a provider on the same leve.

data "digitalocean_kubernetes_cluster" "foreign_language_reader" {
  name = var.cluster_name
}

provider "kubernetes" {
  load_config_file = false
  host             = data.digitalocean_kubernetes_cluster.foreign_language_reader.endpoint
  token            = data.digitalocean_kubernetes_cluster.foreign_language_reader.kube_config[0].token
  cluster_ca_certificate = base64decode(
    data.digitalocean_kubernetes_cluster.foreign_language_reader.kube_config[0].cluster_ca_certificate
  )
}

provider "helm" {
  kubernetes {
    load_config_file = false
    host             = data.digitalocean_kubernetes_cluster.foreign_language_reader.endpoint
    token            = data.digitalocean_kubernetes_cluster.foreign_language_reader.kube_config[0].token
    cluster_ca_certificate = base64decode(
      data.digitalocean_kubernetes_cluster.foreign_language_reader.kube_config[0].cluster_ca_certificate
    )
  }
}

module "container_registries" {
  source = "./container_registries"
}

resource "digitalocean_domain" "main" {
  name = "foreignlanguagereader.com"
}

# The supporting infrastructure, eg. database, load balancer
module "kubernetes_cluster_infrastructure" {
  source           = "./kubernetes_cluster_infrastructure"
  cluster_name     = var.cluster_name
  test_environment = var.test_environment
  domain_name      = digitalocean_domain.main.name
}

# Installed apps, some secrets, service users, etc.
module "kubernetes_config" {
  source              = "./kubernetes_config"
  api_secret_key_base = var.api_secret_key_base
}
