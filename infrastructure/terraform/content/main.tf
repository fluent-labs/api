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

resource "kubernetes_secret" "local_connection_token_content" {
  metadata {
    name      = "local-connection-token"
    namespace = "content"
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
