data "aws_caller_identity" "current" {}

# Codebuild job

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
    type = "CODEPIPELINE"
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
    privileged_mode             = true

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
    type            = "CODEPIPELINE"
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

resource "aws_codepipeline" "foreign_language_reader_pipeline" {
  name     = "foreign-language-reader-pipeline"
  role_arn = var.codepipeline_role

  artifact_store {
    location = aws_s3_bucket.foreign_language_reader_api_build.bucket
    type     = "S3"
  }

  stage {
    name = "Source"

    action {
      name             = "Source"
      category         = "Source"
      owner            = "ThirdParty"
      provider         = "GitHub"
      version          = "1"
      output_artifacts = ["source"]

      configuration = {
        Owner      = "lucaskjaero"
        Repo       = "foreign-language-reader"
        Branch     = "master"
        OAuthToken = var.github_token
      }
    }
  }

  stage {
    name = "Build"

    action {
      name             = "Build"
      category         = "Build"
      owner            = "AWS"
      provider         = "CodeBuild"
      version          = "1"
      input_artifacts  = ["source"]
      output_artifacts = ["imagedefinitions"]

      configuration = {
        ProjectName = aws_codebuild_project.api_build.name
      }
    }
  }

  # TODO database migrations

  stage {
    name = "Production"

    action {
      name            = "Deploy"
      category        = "Deploy"
      owner           = "AWS"
      provider        = "ECS"
      input_artifacts = ["imagedefinitions"]
      version         = "1"

      configuration = {
        ClusterName = var.api_cluster_name
        ServiceName = var.api_service_name
        FileName    = "imagedefinitions.json"
      }
    }
  }
}
