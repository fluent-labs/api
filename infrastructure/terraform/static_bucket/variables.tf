variable "deploy_users" {
  description = "The IAM users to give write permissions to."
}

variable "domain" {
  description = "The domain the site will be hosted on."
}

variable "subdomain" {
  description = "The subdomains the site will be hosted on."
  default     = ""
}
