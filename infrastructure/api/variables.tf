variable "env" {
  description = "Name of the environment"
}

variable "instance_size" {
  description = "Default size of instances created"
}

variable "private_subnet_ids" {
  description = "The first subnet to set up the server on"
}

variable "puhlic_subnet_ids" {
  description = "The second subnet to set up the server on"
}

variable "rds_username" {
  default = "test_username"
}

variable "rds_password" {
  default = "test_username"
}
