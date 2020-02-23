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

data "aws_iam_policy_document" "ecr_user" {
  statement {
    actions   = ["ecr:GetAuthorizationToken"]
    effect    = "Allow"
    resources = ["*"]
  }
}

resource "aws_iam_policy" "ecr_user" {
  name        = "ecr-user"
  description = "IAM policy for getting access to ECR"

  policy = data.aws_iam_policy_document.ecr_user.json
}

resource "aws_iam_policy_attachment" "ecr_user_attach" {
  name       = "ecr_user"
  users      = [aws_iam_user.github.name, aws_iam_user.kubernetes.name]
  policy_arn = aws_iam_policy.ecr_user.arn
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
