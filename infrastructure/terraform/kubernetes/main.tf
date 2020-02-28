resource "kubernetes_secret" "api_secret_key_base" {
  metadata {
    name = "api-secret-key-base"
  }

  data = {
    secret_key_base = var.api_secret_key_base
  }
}

data "helm_repository" "nginx_stable" {
  name = "nginx-stable"
  url  = "https://helm.nginx.com/stable"
}

resource "helm_release" "nginx_ingress" {
  name              = "nginx-ingress"
  repository        = "https://helm.nginx.com/stable"
  chart             = "nginx-stable/nginx-ingress"
  version           = "1.62"
  dependency_update = true
}
