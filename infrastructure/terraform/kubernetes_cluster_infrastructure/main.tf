data "digitalocean_domain" "main" {
  name = var.domain_name
}

resource "digitalocean_project" "foreign_language_reader" {
  name        = "foreign-language-reader"
  description = "Read text in different languages."
  purpose     = "Web Application"
  environment = "Production"
  resources = [
    digitalocean_database_cluster.api_mysql.urn,
    digitalocean_loadbalancer.foreign_language_reader.urn,
  data.digitalocean_domain.main.urn]
}

data "digitalocean_kubernetes_cluster" "foreign_language_reader" {
  name = var.cluster_name
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
    value = data.digitalocean_kubernetes_cluster.foreign_language_reader.id
  }
}

resource "digitalocean_database_user" "api_user" {
  cluster_id = digitalocean_database_cluster.api_mysql.id
  name       = "api"
}

resource "digitalocean_database_db" "api_database" {
  cluster_id = digitalocean_database_cluster.api_mysql.id
  name       = "foreign-language-reader"
}

resource "kubernetes_secret" "api_database_credentials" {
  metadata {
    name = "api-database-credentials"
  }

  data = {
    username          = digitalocean_database_user.api_user.name
    password          = digitalocean_database_user.api_user.password
    host              = digitalocean_database_cluster.api_mysql.private_host
    port              = digitalocean_database_cluster.api_mysql.port
    database          = digitalocean_database_db.api_database.name
    connection_string = "ecto://${digitalocean_database_user.api_user.name}:${digitalocean_database_user.api_user.password}@${digitalocean_database_cluster.api_mysql.private_host}:${digitalocean_database_cluster.api_mysql.port}/${digitalocean_database_db.api_database.name}"
  }
}

# Configure networking

# This is created by K8s and needs to be imported manually
resource "digitalocean_loadbalancer" "foreign_language_reader" {
  name   = "foreign-language-reader"
  region = "sfo2"

  forwarding_rule {
    entry_port      = 80
    entry_protocol  = "tcp"
    target_port     = 30775
    target_protocol = "tcp"
  }

  forwarding_rule {
    entry_port      = 443
    entry_protocol  = "tcp"
    target_port     = 30233
    target_protocol = "tcp"
  }

  healthcheck {
    port                     = 30775
    protocol                 = "tcp"
    check_interval_seconds   = 3
    response_timeout_seconds = 5
  }
}

resource "digitalocean_record" "api" {
  domain = var.domain_name
  type   = "A"
  name   = "api"
  value  = digitalocean_loadbalancer.foreign_language_reader.ip
}

resource "digitalocean_record" "api" {
  domain = var.domain_name
  type   = "A"
  name   = "language"
  value  = digitalocean_loadbalancer.foreign_language_reader.ip
}
