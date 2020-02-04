resource "aws_s3_bucket" "foreign-language-reader-frontend-dev" {
  bucket = "foreign-language-reader-frontend-dev"
  acl    = "public-read"

  website {
    index_document = "index.html"
    error_document = "error.html"
  }
}
