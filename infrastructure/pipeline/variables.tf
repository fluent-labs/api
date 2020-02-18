variable "codebuild_role" {
  description = "IAM role for the codebuild job"
}

variable "codepipeline_role" {
  description = "IAM role for the codepipeline job"
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

variable "cluster_name" {
  description = "The name of the cluster to push containers to"
}

variable "api_ecr_name" {
  description = "The name of the container registry to push api containers to"
}

variable "api_service_name" {
  description = "The name of the service to push api containers to"
}

variable "language_service_ecr_name" {
  description = "The name of the container registry to push language-service containers to"
}

variable "language_service_service_name" {
  description = "The name of the service to push language-service containers to"
}
