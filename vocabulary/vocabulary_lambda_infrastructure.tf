resource "aws_s3_bucket" "vocabulary-lambda-deploy" {
  bucket = "vocabulary-lambda-deploy"
  acl    = "private"
}

data "aws_iam_policy_document" "lambda-assume-role-policy" {
  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["lambda.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "lambda_exec" {
  name               = "foreign-language-reader-vocabulary-lambda"
  assume_role_policy = "${data.aws_iam_policy_document.lambda-assume-role-policy.json}"
}

resource "aws_lambda_function" "foreign-language-reader-vocabulary-lambda" {
  depends_on = [
    aws_s3_bucket.vocabulary-lambda-deploy,
  ]

  function_name = "ForeignLanguageReaderVocabularyLambda"

  s3_bucket = "vocabulary-lambda-deploy"
  s3_key    = "package.zip"

  handler = "service.handler"
  runtime = "python3.6"

  role = aws_iam_role.lambda_exec.arn
}

resource "aws_api_gateway_rest_api" "rest_api" {
  name        = "ForeignLanguageReaderVocabularyLambda"
  description = "Gateway for the foreign language reader vocabulary lambda"
}

resource "aws_api_gateway_resource" "proxy" {
  rest_api_id = aws_api_gateway_rest_api.rest_api.id
  parent_id   = aws_api_gateway_rest_api.rest_api.root_resource_id
  path_part   = "{proxy+}"
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
  stage_name  = "test"
}

resource "aws_lambda_permission" "apigw" {
  statement_id  = "AllowAPIGatewayInvoke"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.foreign-language-reader-vocabulary-lambda.function_name
  principal     = "apigateway.amazonaws.com"

  source_arn = "${aws_api_gateway_rest_api.rest_api.execution_arn}/*/*"
}
