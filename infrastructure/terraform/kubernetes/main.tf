resource "kubernetes_secret" "api_secret_key_base" {
  metadata {
    name = "api-secret-key-base"
  }

  data = {
    secret_key_base = var.api_secret_key_base
  }
}

resource "helm_release" "nginx_ingress" {
  name              = "nginx-ingress"
  repository        = "https://helm.nginx.com/stable"
  chart             = "nginx-stable/nginx-ingress"
  version           = "0.4.2"
  dependency_update = true
}
