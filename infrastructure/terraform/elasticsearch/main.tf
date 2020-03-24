# Production elasticsearch setup
# Used to store language content and also logs.


# Healthcheck user

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

resource "random_password" "kibana_encryption_key" {
  length      = 32
  special     = false
  min_numeric = 10
}

# Elasticsearch

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

# Kibana

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
