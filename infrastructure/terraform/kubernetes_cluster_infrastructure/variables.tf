variable "test_environment" {
  description = "Whether to build a test environment"
}

variable "cluster_name" {
  description = "The name of the K8s cluster to deploy things on"
}

variable "domain_name" {
  description = "The domain name to attach DNS records to"
}
