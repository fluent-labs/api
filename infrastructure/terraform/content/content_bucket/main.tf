data "aws_caller_identity" "current" {}

resource "aws_s3_bucket" "main" {
  bucket = "foreign-language-reader-${var.name}"
  acl    = "private"
}

# TODO give permissions for jobs to use this.
