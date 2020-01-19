terraform {
  backend "remote" {
    hostname = "app.terraform.io"
    organization = "foreign-language-reader"

    workspaces {
      name = "foreign-language-reader"
    }
  }
}

provider "aws" {
  profile    = "default"
  region     = "us-west-1"
}
resource "aws_iam_role" "lambda_exec" {
   name = "foreign_language_reader_api"

   assume_role_policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Action": "sts:AssumeRole",
      "Principal": {
        "Service": "lambda.amazonaws.com"
      },
      "Effect": "Allow",
      "Sid": ""
    },
    {
      "Effect": "Allow",
      "Action": [
          "logs:CreateLogGroup",
          "logs:CreateLogStream",
          "logs:PutLogEvents"
      ],
      "Resource": "*"
    }
  ]
}
 EOF

 }

resource "aws_lambda_function" "foreign-language-reader-api" {
  function_name = "ForeignLanguageReaderAPI"

  s3_bucket = "foreign-language-reader-deploy"
  s3_key    = "package.zip"

  handler = "graphql.graphqlHandler"
  runtime = "nodejs12.x"

  role = aws_iam_role.lambda_exec.arn
}

resource "aws_api_gateway_rest_api" "rest_api" {
  name        = "ForeignLanguageReaderAPI"
  description = "Gateway for the foreign language reader API"
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
   uri                     = aws_lambda_function.foreign-language-reader-api.invoke_arn
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
   function_name = aws_lambda_function.foreign-language-reader-api.function_name
   principal     = "apigateway.amazonaws.com"

   source_arn = "${aws_api_gateway_rest_api.rest_api.execution_arn}/*/*"
 }
