# Push enabled user for github

resource "aws_iam_access_key" "github" {
  user = aws_iam_user.github.name
}

resource "aws_iam_user" "github" {
  name = "foreign-language-reader-github"
}

# Read only pull user for K8s

resource "aws_iam_access_key" "kubernetes" {
  user = aws_iam_user.kubernetes.name
}

resource "aws_iam_user" "kubernetes" {
  name = "foreign-language-reader-kubernetes"
}

# Give K8s credentials

data "digitalocean_kubernetes_cluster" "foreign_language_reader" {
  name = var.cluster_name
}

provider "kubernetes" {
  load_config_file = false
  host             = data.digitalocean_kubernetes_cluster.foreign_language_reader.endpoint
  token            = data.digitalocean_kubernetes_cluster.foreign_language_reader.kube_config[0].token
  cluster_ca_certificate = base64decode(
    data.digitalocean_kubernetes_cluster.foreign_language_reader.kube_config[0].cluster_ca_certificate
  )
}

data "aws_caller_identity" "current" {}

resource "kubernetes_secret" "kubernetes_user_secret" {
  metadata {
    name = "aws"
  }

  data = {
    AWS_ACCOUNT_NUMBER    = data.aws_caller_identity.current.account_id
    AWS_ACCESS_KEY_ID     = aws_iam_access_key.kubernetes.id
    AWS_SECRET_ACCESS_KEY = aws_iam_access_key.kubernetes.secret
  }
}

# Generic policy attachment allowing auth against ECR

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

# Container registries for use in this app.

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
