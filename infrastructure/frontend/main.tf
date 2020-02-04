resource "aws_s3_bucket" "foreign-language-reader-frontend" {
  bucket = "foreign-language-reader-frontend-${var.env}"
  acl    = "public-read"

  website {
    index_document = "index.html"
    error_document = "error.html"
  }
}
