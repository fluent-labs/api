data "aws_caller_identity" "current" {}

# Codebuild job

resource "aws_codebuild_source_credential" "github" {
  auth_type   = "PERSONAL_ACCESS_TOKEN"
  server_type = "GITHUB"
  token       = var.github_token
}

resource "aws_s3_bucket" "foreign_language_reader_api_build" {
  bucket = "foreign-language-reader-api-build"
  acl    = "private"
}

resource "aws_security_group" "codebuild" {
  name        = "foreign-language-reader-codebuild"
  description = "Codebuild worker security group"
  vpc_id      = var.vpc_id

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "build-api-foreign-language-reader"
  }
}

resource "aws_codebuild_project" "api_build" {
  name          = "foreign-language-reader-api"
  description   = "The build job for the foreign language reader"
  build_timeout = "5"
  service_role  = var.codebuild_role

  artifacts {
    type = "NO_ARTIFACTS"
  }

  cache {
    type     = "S3"
    location = aws_s3_bucket.foreign_language_reader_api_build.bucket
  }

  environment {
    compute_type                = "BUILD_GENERAL1_SMALL"
    image                       = "aws/codebuild/standard:2.0"
    type                        = "LINUX_CONTAINER"
    image_pull_credentials_type = "CODEBUILD"

    environment_variable {
      name  = "AWS_ACCOUNT_ID"
      value = data.aws_caller_identity.current.account_id
    }

    environment_variable {
      name  = "IMAGE_REPO_NAME"
      value = var.api_ecr_name
    }

  }

  logs_config {
    s3_logs {
      status   = "ENABLED"
      location = "${aws_s3_bucket.foreign_language_reader_api_build.id}/build-log"
    }
  }

  source {
    buildspec       = "api/buildspec.yml"
    type            = "GITHUB"
    location        = "https://github.com/lucaskjaero/foreign-language-reader.git"
    git_clone_depth = 1
  }

  vpc_config {
    vpc_id  = var.vpc_id
    subnets = var.private_subnet_ids

    security_group_ids = [aws_security_group.codebuild.id]
  }

  tags = {
    Environment = "Build"
  }
}

resource "aws_codebuild_webhook" "github_webhook" {
  project_name = aws_codebuild_project.api_build.name

  filter_group {
    filter {
      type    = "EVENT"
      pattern = "PUSH"
    }

    filter {
      type    = "HEAD_REF"
      pattern = "master"
    }
  }
}
