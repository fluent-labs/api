resource "aws_iam_access_key" "github" {
  user = aws_iam_user.github.name
}

resource "aws_iam_user" "github" {
  name = "foreign-language-reader-github"
}

resource "aws_iam_access_key" "kubernetes" {
  user = aws_iam_user.kubernetes.name
}

resource "aws_iam_user" "kubernetes" {
  name = "foreign-language-reader-kubernetes"
}

module "api_registry" {
  source      = "./container_registry"
  name        = "foreign-language-reader-api"
  image_count = 5
  push_users  = [aws_iam_user.github.name]
  pull_users  = [aws_iam_user.kubernetes.name]
}

module "language_service_registry" {
  source      = "./container_registry"
  name        = "foreign-language-reader-language-service"
  image_count = 5
  push_users  = [aws_iam_user.github.name]
  pull_users  = [aws_iam_user.kubernetes.name]
}
