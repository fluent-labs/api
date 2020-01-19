provider "aws" {
  profile    = "default"
  region     = "us-west-1"
}

resource "aws_lambda_function" "foreign-language-reader-api" {
  function_name = "ForeignLanguageReaderAPI"

  s3_bucket = "terraform-serverless-example"
  s3_key    = "v1.0.0/example.zip"

  handler = "graphql.graphqlHandler"
  runtime = "nodejs12.x"

  role = aws_iam_role.lambda_exec.arn
}

resource "aws_iam_role" "lambda_exec" {
   name = "foreign_language_reader_lambda"

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
     }
   ]
 }
 EOF

 }
