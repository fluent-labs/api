variable "env" {
  description = "Name of the environment"
}

variable "instance_size" {
  description = "Default size of instances created"
}

variable "subnet_id_one" {
  description = "The first subnet to set up the server on"
}

variable "subnet_id_two" {
  description = "The second subnet to set up the server on"
}

variable "rds_username" {
  default = "test_username"
}

variable "rds_password" {
  default = "test_username"
}
