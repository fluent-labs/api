variable "env" {
  description = "Name of the environment"
}

variable "instance_size" {
  description = "Default size of instances created"
}

variable "cidr_block" {
  description = "What CIDR block to use"
}

variable "subnet_cidr_blocks" {
  description = "CIDR blocks to create subnets in"
}
