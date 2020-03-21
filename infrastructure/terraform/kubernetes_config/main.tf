# Token used for connecting between services
resource "random_password" "local_connection_token" {
  length  = 64
  special = true
}

resource "kubernetes_secret" "local_connection_token" {
  metadata {
    name = "local-connection-token"
  }

  data = {
    local_connection_token = random_password.local_connection_token.result
  }
}

resource "random_password" "api_secret_key_base" {
  length  = 64
  special = true
}

resource "kubernetes_secret" "api_secret_key_base" {
  metadata {
    name = "api-secret-key-base"
  }

  data = {
    secret_key_base = random_password.api_secret_key_base.result
  }
}

# Configure NGinx proxy for connecting to all services

resource "kubernetes_secret" "nginx_certificate" {
  metadata {
    name = "nginx-certificate"
  }

  data = {
    "tls.key" = var.private_key_pem
    "tls.crt" = <<EOF
${var.certificate_pem}
${var.issuer_pem}
EOF
  }

  type = "kubernetes.io/tls"
}

resource "helm_release" "nginx_ingress" {
  name       = "nginx-ingress"
  repository = "https://kubernetes-charts.storage.googleapis.com/"
  chart      = "nginx-ingress"
  version    = "1.33.0"
}

# Production elasticsearch setup
# Used to store language content and also logs.

resource "random_password" "elasticsearch_password" {
  length      = 32
  special     = false
  min_numeric = 10
}

resource "kubernetes_secret" "elastic_credentials" {
  metadata {
    name = "elastic-credentials"
  }

  data = {
    username      = "healthcheck"
    password      = random_password.elasticsearch_password.result
    encryptionkey = random_password.kibana_encryption_key.result
  }
}

resource "random_password" "kibana_encryption_key" {
  length      = 32
  special     = false
  min_numeric = 10
}

resource "kubernetes_secret" "elasticsearch_internal_certificate" {
  metadata {
    name = "elasticsearch-certificates"
  }

  data = {
    "private_key_pem.crt" = var.private_key_pem
    "certificate_pem.crt" = var.certificate_pem
    "issuer_pem.crt"      = var.issuer_pem
  }
}

resource "helm_release" "elasticsearch" {
  name       = "elasticsearch"
  repository = "https://helm.elastic.co"
  chart      = "elasticsearch"
  version    = "7.6.1"

  values = [file("${path.module}/elasticsearch.yml")]

  depends_on = [
    kubernetes_secret.elasticsearch_internal_certificate,
    kubernetes_secret.elastic_credentials
  ]
}

resource "helm_release" "kibana" {
  name       = "kibana"
  repository = "https://helm.elastic.co"
  chart      = "kibana"
  version    = "7.6.1"

  values = [file("${path.module}/kibana.yml")]

  depends_on = [
    kubernetes_secret.elastic_credentials
  ]
}

# Application credentials for elasticsearch
resource "random_password" "language_service_elasticsearch_password" {
  length      = 32
  special     = false
  min_numeric = 10
}

resource "kubernetes_secret" "language_service_elastic_credentials" {
  metadata {
    name = "language-service-elastic-credentials"
  }

  data = {
    username = "languageservice"
    password = random_password.language_service_elasticsearch_password.result
  }
}

# Logging configuration
# Every node has a log collection agent that posts logs to elasticsearch

resource "kubernetes_namespace" "logging" {
  metadata {
    annotations = {
      name = "logging"
    }

    name = "logging"
  }
}

resource "random_password" "fluent_elasticsearch_password" {
  length      = 32
  special     = false
  min_numeric = 10
}

resource "helm_release" "fluentd_elasticsearch" {
  name       = "fluentd"
  repository = "https://kiwigrid.github.io"
  chart      = "fluentd-elasticsearch"
  version    = "6.2.2"
  namespace  = "logging"

  values = [file("${path.module}/fluentd.yml")]

  set_sensitive {
    name  = "elasticsearch.auth.password"
    value = random_password.fluent_elasticsearch_password.result
  }

  depends_on = [
    kubernetes_namespace.logging
  ]
}

# Content jobs runtime
# Content requires long running jobs that could be split into parallel
# This is a good use case for spark.

resource "kubernetes_namespace" "content" {
  metadata {
    annotations = {
      name = "content"
    }

    name = "content"
  }
}

resource "helm_release" "spark" {
  name       = "spark"
  repository = "https://charts.bitnami.com/bitnami"
  chart      = "spark"
  version    = "1.2.10"
  namespace  = "content"

  depends_on = [
    kubernetes_namespace.content
  ]
}
