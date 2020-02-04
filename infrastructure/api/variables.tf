variable "env" {
  description = "Name of the environment"
}

variable "instance_size" {
  description = "Default size of instances created"
}

variable "subnet_id" {
  description = "The subnet to set up the server on"
}

variable "rds_username" {
  default = "test_username"
}

variable "rds_password" {
  default = "test_username"
}
