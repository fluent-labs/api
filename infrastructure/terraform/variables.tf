variable "test_environment" {
  description = "Whether to build a test environment"
}

variable "cluster_name" {
  description = "The name of the K8s cluster to post secrets to"
}

variable "api_secret_key_base" {
  description = "The secret key base to use in the API encryption"
}
