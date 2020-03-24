resource "kubernetes_service" "api" {
  metadata {
    name = "language-service"
  }
  spec {
    selector = {
      app = "language-service"
    }
    port {
      port = 8000
    }
    type = "ClusterIP"
  }
}

resource "kubernetes_horizontal_pod_autoscaler" "example" {
  metadata {
    name = "language-service"
  }
  spec {
    min_replicas = var.min_replicas
    max_replicas = var.max_replicas
    scale_target_ref {
      kind = "Deployment"
      name = "language-service"
    }
    target_cpu_utilization_percentage = 75
  }
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
