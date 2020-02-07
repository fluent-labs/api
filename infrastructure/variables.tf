variable "env" {
  description = "Name of the environment"
}

variable "instance_size" {
  description = "Default size of instances created"
}

variable "cpu" {
  description = "Default size of instances created"
}

variable "memory" {
  description = "Default size of instances created"
}

variable "cidr_block" {
  description = "What CIDR block to use"
}

variable "rds_username" {
  description = "The database username to use for the api"
}

variable "rds_password" {
  description = "The database password to use for the api"
}

variable "secret_key_base" {
  description = "The key to use for encryption within the service"
}

variable "github_token" {
  description = "The github token to use when building projects in this repo"
}
