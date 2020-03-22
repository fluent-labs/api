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

# Deploy user for github actions
# Will be given ECR push and S3 sync access
resource "aws_iam_access_key" "github" {
  user = aws_iam_user.github.name
}

resource "aws_iam_user" "github" {
  name = "foreign-language-reader-github"
}

module "container_registries" {
  source                = "./container_registries"
  push_users            = [aws_iam_user.github.name]
  kubernetes_namespaces = ["default", "content"]
}

resource "digitalocean_domain" "main" {
  name = "foreignlanguagereader.com"
}

# The supporting cloud infrastructure, eg. database, load balancer
module "kubernetes_cluster_infrastructure" {
  source           = "./kubernetes_cluster_infrastructure"
  cluster_name     = var.cluster_name
  test_environment = var.test_environment
  domain_name      = digitalocean_domain.main.name
}

# Configuration within K8s: installed apps, some secrets, service users, etc.
module "kubernetes_config" {
  source          = "./kubernetes_config"
  private_key_pem = acme_certificate.certificate.private_key_pem
  certificate_pem = acme_certificate.certificate.certificate_pem
  issuer_pem      = acme_certificate.certificate.issuer_pem
}

module "frontend" {
  source       = "./static_bucket"
  domain       = digitalocean_domain.main.name
  subdomain    = "www"
  deploy_users = [aws_iam_user.github.name]
}

module "storybook" {
  source       = "./static_bucket"
  domain       = digitalocean_domain.main.name
  subdomain    = "storybook"
  deploy_users = [aws_iam_user.github.name]
}

# Section to create TLS certs

resource "tls_private_key" "tls_private_key" {
  algorithm = "RSA"
}

resource "acme_registration" "reg" {
  account_key_pem = tls_private_key.tls_private_key.private_key_pem
  email_address   = "letsencrypt@lucaskjaerozhang.com"
}

resource "acme_certificate" "certificate" {
  account_key_pem = acme_registration.reg.account_key_pem
  common_name     = "*.foreignlanguagereader.com"

  dns_challenge {
    provider = "digitalocean"
    config = {
      DO_AUTH_TOKEN          = var.digitalocean_token
      DO_HTTP_TIMEOUT        = 60
      DO_POLLING_INTERVAL    = 30
      DO_PROPAGATION_TIMEOUT = 600
    }
  }
}
