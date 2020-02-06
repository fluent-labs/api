variable "env" {
  description = "Name of the environment"
}

variable "instance_size" {
  description = "Default size of instances created"
}

variable "vpc_id" {
  description = "The id of the VPC this app will be installed in"
}

variable "private_subnet_ids" {
  description = "The private subnet ids to install this service on"
}

variable "public_subnet_ids" {
  description = "The public subnet ids to install the load balancer on"
}

variable "rds_username" {
  default = "test_username"
}

variable "rds_password" {
  default = "test_username"
}
