output "api_role" {
  value = aws_iam_role.api_task_exec.arn
}

output "codebuild_role" {
  value = aws_iam_role.codebuild_role.arn
}
