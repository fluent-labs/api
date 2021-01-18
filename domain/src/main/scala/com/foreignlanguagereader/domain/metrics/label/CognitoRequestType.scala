package com.foreignlanguagereader.domain.metrics.label

object CognitoRequestType extends Enumeration {
  type CognitoRequestType = Value

  val INITIATE_AUTH_REQUEST: Value = Value("initiate_auth_request")
  val RESPOND_TO_AUTH_CHALLENGE: Value = Value("respond_to_auth_challenge")
  val SIGNUP: Value = Value("signup")
}
