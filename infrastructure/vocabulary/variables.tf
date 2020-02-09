variable "env" {
  description = "Name of the environment"
}

variable "vocabulary_deploy_bucket" {
  description = "The bucket to deploy the lambda from"
}

variable "vocabulary_role" {
  description = "The role to run the lambda as"
}
