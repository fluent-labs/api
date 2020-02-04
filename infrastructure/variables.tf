variable "env" {
  description = "Name of the environment"
}

variable "instance_size" {
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
