variable "env" {
  description = "Name of the environment"
}

variable "iam_role" {
  description = "IAM role for the api container"
}

variable "fargate_autoscale_role" {
  description = "IAM role for the container autoscaling"
}

variable "cluster_name" {
  description = "The cluster to create the fargate services on"
}

variable "cpu" {
  description = "Default size of instances created"
}

variable "memory" {
  description = "Default size of instances created"
}

variable "default_capacity" {
  description = "Default number of instances created"
}

variable "min_capacity" {
  description = "Minimum number of instances created"
}

variable "max_capacity" {
  description = "Maximum number of instances created"
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
