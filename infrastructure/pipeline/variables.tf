variable "codebuild_role" {
  description = "IAM role for the codebuild job"
}

variable "vpc_id" {
  description = "The id of the VPC this app will be installed in"
}

variable "private_subnet_ids" {
  description = "The private subnet ids to install this service on"
}

variable "github_token" {
  description = "The github token to use when building projects in this repo"
}

variable "api_ecr_name" {
  description = "The name of the container registry to push api containers to"
}
