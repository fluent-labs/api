output "ecr_name" {
  value = aws_ecr_repository.foreign_language_reader_language_service.name
}

output "service_name" {
  value = aws_ecs_service.language_service.name
}
