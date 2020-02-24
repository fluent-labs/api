# Configure K8s

resource "digitalocean_project" "foreign_language_reader" {
  name        = "foreign-language-reader"
  description = "Read text in different languages."
  purpose     = "Web Application"
  environment = "Production"
}

resource "digitalocean_kubernetes_cluster" "foreign_language_reader" {
  name    = "foreign-language-reader"
  region  = "sfo2"
  version = "1.16.6-do.0"
  tags    = ["prod"]

  node_pool {
    name       = "worker-pool"
    size       = "s-1vcpu-2gb"
    auto_scale = true
    min_nodes  = 1
    max_nodes  = 2
  }
}

provider "kubernetes" {
  load_config_file = false
  host             = digitalocean_kubernetes_cluster.foreign_language_reader.endpoint
  token            = digitalocean_kubernetes_cluster.foreign_language_reader.kube_config[0].token
  cluster_ca_certificate = base64decode(
    digitalocean_kubernetes_cluster.foreign_language_reader.kube_config[0].cluster_ca_certificate
  )
}

# Configure database

resource "digitalocean_database_cluster" "api_mysql" {
  name       = "foreign-language-reader"
  engine     = "mysql"
  version    = "8"
  size       = "db-s-1vcpu-1gb"
  region     = "sfo2"
  node_count = 1
}

resource "digitalocean_database_firewall" "allow_kubernetes" {
  cluster_id = digitalocean_database_cluster.api_mysql.id

  rule {
    type  = "k8s"
    value = digitalocean_kubernetes_cluster.foreign_language_reader.id
  }
}

resource "digitalocean_database_user" "api_user" {
  cluster_id = digitalocean_database_cluster.api_mysql.id
  name       = "api"
}

resource "digitalocean_database_db" "api_database" {
  cluster_id = digitalocean_database_cluster.foreign_language_reader.id
  name       = "foreign-language-reader"
}

resource "kubernetes_secret" "example" {
  metadata {
    name = "api-database-credentials"
  }

  data = {
    username = digitalocean_database_user.api_user.name
    password = digitalocean_database_user.api_user.password
  }
}
