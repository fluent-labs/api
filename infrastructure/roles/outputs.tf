output "api_role" {
  value = aws_iam_role.api_task_exec.arn
}

output "codebuild_role" {
  value = aws_iam_role.codebuild_role.arn
}

output "codepipeline_role" {
  value = aws_iam_role.codepipeline_role.arn
}

output "vocabulary_role" {
  value = aws_iam_role.vocabulary_lambda_exec.arn
}
