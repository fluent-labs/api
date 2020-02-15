output "ecr_name" {
  value = aws_ecr_repository.foreign_language_reader_api.name
}

output "service_name" {
  value = aws_ecs_service.api.name
}
