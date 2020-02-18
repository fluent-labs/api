output "fargate_role" {
  value = aws_iam_role.fargate_task_exec.arn
}

output "codebuild_role" {
  value = aws_iam_role.codebuild_role.arn
}

output "codepipeline_role" {
  value = aws_iam_role.codepipeline_role.arn
}
