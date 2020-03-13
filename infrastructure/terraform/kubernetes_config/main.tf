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

resource "helm_release" "nginx_ingress" {
  name       = "nginx-ingress"
  repository = "https://kubernetes-charts.storage.googleapis.com/"
  chart      = "nginx-ingress"
  version    = "1.33.0"
}

resource "kubernetes_namespace" "cert_manager" {
  metadata {
    name = "cert-manager"
  }
}

resource "helm_release" "cert_manager" {
  name       = "cert-manager"
  repository = "https://charts.jetstack.io"
  chart      = "cert-manager"
  version    = "v0.13.1"
  namespace  = "cert-manager"
}

resource "kubernetes_namespace" "logging" {
  metadata {
    name = "logging"
  }
}

resource "helm_release" "elasticsearch" {
  name       = "elasticsearch"
  repository = "https://kubernetes-charts.storage.googleapis.com"
  chart      = "elasticsearch"
  version    = "1.32.4"
  namespace  = "logging"
  timeout    = 1200

  set {
    name  = "client.replicas"
    value = 1
  }

  set {
    name  = "master.replicas"
    value = 1
  }

  set {
    name  = "data.replicas"
    value = 1
  }
}

resource "helm_release" "kibana" {
  name       = "kibana"
  repository = "https://kubernetes-charts.storage.googleapis.com"
  chart      = "kibana"
  version    = "3.2.6"
  namespace  = "logging"
}
