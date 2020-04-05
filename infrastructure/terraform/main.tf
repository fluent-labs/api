# Hold K8s configuration in an intermediate level
# Terraform currently cannot create a cluster and use it to set up a provider on the same leve.

data "digitalocean_kubernetes_cluster" "foreign_language_reader" {
  name = var.cluster_name
}

# provider "kubernetes" {
#   load_config_file = false
#   host             = data.digitalocean_kubernetes_cluster.foreign_language_reader.endpoint
#   token            = data.digitalocean_kubernetes_cluster.foreign_language_reader.kube_config[0].token
#   cluster_ca_certificate = base64decode(
#     data.digitalocean_kubernetes_cluster.foreign_language_reader.kube_config[0].cluster_ca_certificate
#   )
# }
#
# provider "helm" {
#   kubernetes {
#     load_config_file = false
#     host             = data.digitalocean_kubernetes_cluster.foreign_language_reader.endpoint
#     token            = data.digitalocean_kubernetes_cluster.foreign_language_reader.kube_config[0].token
#     cluster_ca_certificate = base64decode(
#       data.digitalocean_kubernetes_cluster.foreign_language_reader.kube_config[0].cluster_ca_certificate
#     )
#   }
# }

# Service container registries
module "container_registries" {
  source                = "./container_registries"
  push_users            = [aws_iam_user.github.name]
  kubernetes_namespaces = ["default", "content"]
}

# Static content served to users

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

# Services

# module "api" {
#   source       = "./api"
#   cluster_name = var.cluster_name
#   min_replicas = 2
#   max_replicas = 10
# }
#
# module "language_service" {
#   source       = "./language_service"
#   min_replicas = 2
#   max_replicas = 10
# }

# Full ELK stack for storing language content
# Also uses fluentd to post cluster logs to ELK to make this work
# module "elasticsearch" {
#   source          = "./elasticsearch"
#   private_key_pem = acme_certificate.certificate.private_key_pem
#   certificate_pem = acme_certificate.certificate.certificate_pem
#   issuer_pem      = acme_certificate.certificate.issuer_pem
# }

# Content infrastructure
# Spark jobs that scrape wiktionary for definitions
# Should also have job triggers
# And potentially example sentences in the future

# module "content" {
#   source = "./content"
# }
#
# # Pretty much prometheus configuration
# module "monitoring" {
#   source = "./monitoring"
# }

# Ingress
# Handles traffic going in to the cluster
# Proxies everything through a load balancer and nginx

# module "nginx_ingress" {
#   source          = "./nginx_ingress"
#   domain          = digitalocean_domain.main.name
#   subdomains      = ["api", "kibana", "language"]
#   private_key_pem = acme_certificate.certificate.private_key_pem
#   certificate_pem = acme_certificate.certificate.certificate_pem
#   issuer_pem      = acme_certificate.certificate.issuer_pem
# }
#
# resource "kubernetes_ingress" "foreign_language_reader_ingress" {
#   metadata {
#     name = "foreign-language-reader-ingress"
#     annotations = {
#       "kubernetes.io/ingress.class"             = "nginx"
#       "nginx.ingress.kubernetes.io/enable-cors" = "true"
#     }
#   }
#
#   spec {
#     tls {
#       secret_name = "nginx-certificate"
#     }
#
#     rule {
#       host = "api.foreignlanguagereader.com"
#       http {
#         path {
#           backend {
#             service_name = "api"
#             service_port = 4000
#           }
#         }
#       }
#     }
#
#     rule {
#       host = "language.foreignlanguagereader.com"
#       http {
#         path {
#           backend {
#             service_name = "language-service"
#             service_port = 8000
#           }
#         }
#       }
#     }
#
#     rule {
#       host = "kibana.foreignlanguagereader.com"
#       http {
#         path {
#           backend {
#             service_name = "kibana-kibana"
#             service_port = 5601
#           }
#         }
#       }
#     }
#   }
# }

# Shared resources for the cluster go down here.

resource "digitalocean_domain" "main" {
  name = "foreignlanguagereader.com"
}

# Deploy user for github actions
# Will be given ECR push and S3 sync access
resource "aws_iam_access_key" "github" {
  user = aws_iam_user.github.name
}

resource "aws_iam_user" "github" {
  name = "foreign-language-reader-github"
}

# Token used for connecting between services
resource "random_password" "local_connection_token" {
  length  = 64
  special = true
}

# resource "kubernetes_secret" "local_connection_token" {
#   for_each = toset(["default", "content"])
#
#   metadata {
#     name      = "local-connection-token"
#     namespace = each.value
#   }
#
#   data = {
#     local_connection_token = random_password.local_connection_token.result
#   }
# }

# TLS

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
