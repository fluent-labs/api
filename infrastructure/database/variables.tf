variable "vpc_id" {
  description = "The id of the VPC this app will be installed in"
}

variable "private_subnet_ids" {
  description = "The private subnet ids to install this service on"
}

variable "instance_size" {
  description = "Default size of instances created"
}

variable "rds_username" {
  default = "test_username"
}

variable "rds_password" {
  default = "test_username"
}
