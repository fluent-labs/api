data "aws_caller_identity" "current" {}

resource "kubernetes_service" "language_service" {
  metadata {
    name      = "language-service"
    namespace = var.env
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

resource "kubernetes_horizontal_pod_autoscaler" "language_service_autoscale" {
  metadata {
    name      = "language-service"
    namespace = var.env
  }
  spec {
    min_replicas = var.min_replicas
    max_replicas = var.max_replicas
    scale_target_ref {
      api_version = "apps/v1"
      kind        = "Deployment"
      name        = "language-service"
    }
    target_cpu_utilization_percentage = 75
  }
}

resource "kubernetes_deployment" "language_service" {
  metadata {
    name      = "language-service"
    namespace = var.env
  }

  spec {
    selector {
      match_labels = {
        app = "language-service"
      }
    }

    template {
      metadata {
        labels = {
          app = "language-service"
        }
      }

      spec {
        image_pull_secrets {
          name = "aws-registry"
        }

        container {
          image = "${data.aws_caller_identity.current.account_id}.dkr.ecr.us-west-2.amazonaws.com/foreign-language-reader-language-service:latest"
          name  = "language-service"

          env {
            name = "AUTH_TOKEN"
            value_from {
              secret_key_ref {
                name = "local-connection-token"
                key  = "local_connection_token"
              }
            }
          }

          env {
            name  = "ELASTICSEARCH_URL"
            value = "http://language-content-es-http.default.svc.cluster.local:9200"
          }

          env {
            name = "ELASTICSEARCH_USERNAME"
            value_from {
              secret_key_ref {
                name = "language-service-elastic-credentials"
                key  = "username"
              }
            }
          }

          env {
            name = "ELASTICSEARCH_PASSWORD"
            value_from {
              secret_key_ref {
                name = "language-service-elastic-credentials"
                key  = "password"
              }
            }
          }

          port {
            container_port = 8000
          }

          resources {
            limits {
              memory = "1.2Gi"
            }
            requests {
              memory = "800Mi"
            }
          }

          liveness_probe {
            http_get {
              path = "/health"
              port = 8000
            }

            initial_delay_seconds = 30
            period_seconds        = 10
            timeout_seconds       = 5
            failure_threshold     = 5
          }

          readiness_probe {
            http_get {
              path = "/health"
              port = 8000
            }

            initial_delay_seconds = 30
            period_seconds        = 10
            timeout_seconds       = 5
            failure_threshold     = 5
          }
        }
      }
    }
  }

  # This resource is to make sure the deployment exists
  # Not blow away what's current for something that doesn't exist.
  lifecycle {
    ignore_changes = [
      spec.0.template.0.spec.0.container.0.image,
      spec.0.replicas
    ]
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
    namespace = var.env
    name      = "language-service-elastic-credentials"
  }

  data = {
    username = "languageservice"
    password = random_password.language_service_elasticsearch_password.result
  }
}
