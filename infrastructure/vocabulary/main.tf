# Deployment bucket

resource "aws_s3_bucket" "vocabulary-lambda-deploy" {
  bucket = var.vocabulary_deploy_bucket
  acl    = "private"
}

# Function

resource "aws_lambda_function" "foreign-language-reader-vocabulary-lambda" {
  function_name = "wiktionary-vocabulary-lookup-${var.env}"
  description   = "Wiktionary vocabulary lookup"

  filename = "infrastructure/vocabulary/package.zip"

  handler = "service.handler"
  runtime = "python3.7"

  timeout     = 30
  memory_size = 512

  role = var.vocabulary_role
}

# API Gateway

resource "aws_api_gateway_rest_api" "rest_api" {
  name        = "foreign-language-reader-vocabulary-lambda-${var.env}"
  description = "Gateway for the foreign language reader vocabulary lambda"
}

resource "aws_api_gateway_resource" "proxy" {
  rest_api_id = aws_api_gateway_rest_api.rest_api.id
  parent_id   = aws_api_gateway_rest_api.rest_api.root_resource_id
  path_part   = "wiktionary-vocabulary-lookup-${var.env}"
}

resource "aws_api_gateway_method" "proxy" {
  rest_api_id   = aws_api_gateway_rest_api.rest_api.id
  resource_id   = aws_api_gateway_resource.proxy.id
  http_method   = "ANY"
  authorization = "NONE"
}

resource "aws_api_gateway_integration" "lambda" {
  rest_api_id = aws_api_gateway_rest_api.rest_api.id
  resource_id = aws_api_gateway_method.proxy.resource_id
  http_method = aws_api_gateway_method.proxy.http_method

  integration_http_method = "POST"
  type                    = "AWS_PROXY"
  uri                     = aws_lambda_function.foreign-language-reader-vocabulary-lambda.invoke_arn
}

resource "aws_api_gateway_deployment" "deployment" {
  depends_on = [
    aws_api_gateway_integration.lambda,
  ]

  rest_api_id = aws_api_gateway_rest_api.rest_api.id
  stage_name  = var.env
}

resource "aws_lambda_permission" "apigw" {
  statement_id  = "AllowAPIGatewayInvoke"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.foreign-language-reader-vocabulary-lambda.function_name
  principal     = "apigateway.amazonaws.com"

  source_arn = "${aws_api_gateway_rest_api.rest_api.execution_arn}/*/*"
}
