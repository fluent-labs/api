package com.foreignlanguagereader.domain.client.aws

import akka.actor.ActorSystem
import com.foreignlanguagereader.domain.client.common.{
  CircuitBreakerResult,
  Circuitbreaker
}
import com.foreignlanguagereader.domain.metrics.MetricsReporter
import com.foreignlanguagereader.domain.metrics.label.CognitoRequestType
import play.api.Logger
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient
import software.amazon.awssdk.services.cognitoidentityprovider.model.{
  AttributeType,
  AuthFlowType,
  InitiateAuthRequest,
  InitiateAuthResponse,
  SignUpRequest,
  SignUpResponse
}

import java.nio.charset.StandardCharsets
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import javax.inject.{Inject, Singleton}
import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AWSCognitoClient @Inject() (
    metrics: MetricsReporter,
    implicit val ec: ExecutionContext,
    val system: ActorSystem
) {
  // Secret, generate in AWS SDK
  val clientId = ""
  val secretKey = ""

  val HMAC_SHA256_ALGORITHM = "HmacSHA256"

  val logger: Logger = Logger(this.getClass)
  val breaker: Circuitbreaker = new Circuitbreaker(system, ec, "Cognito")
  val identityProviderClient: CognitoIdentityProviderClient =
    CognitoIdentityProviderClient
      .builder()
      .region(Region.US_WEST_2)
      .build()

  val logger: Logger = Logger(this.getClass)
  val breaker: Circuitbreaker = new Circuitbreaker(system, ec, "Cognito")

  def signup(
      email: String,
      password: String
  ): Future[CircuitBreakerResult[SignUpResponse]] = {
    val timer = metrics.reportCognitoRequestStarted(CognitoRequestType.SIGNUP)
    breaker.withBreaker(e => {
      metrics.reportCognitoFailure(timer, CognitoRequestType.SIGNUP)
      logger.error(s"Failed to create user $email: ${e.getMessage}", e)
    }) {
      Future {
        val result =
          identityProviderClient.signUp(makeSignupRequest(email, password))
        metrics.reportCognitoRequestFinished(timer)
        result
      }
    }
  }

  def signup(
      email: String,
      password: String
  ): Future[CircuitBreakerResult[SignUpResponse]] =
    performCall(
      () => identityProviderClient.signUp(makeSignupRequest(email, password)),
      CognitoRequestType.SIGNUP,
      e => s"Failed to create user $email: ${e.getMessage}"
    )

  private[this] def makeSignupRequest(email: String, password: String) = {
    val attributes = List(
      AttributeType
        .builder()
        .name("email")
        .value(email)
        .build()
    ).asJava
    SignUpRequest
      .builder()
      .userAttributes(attributes)
      .username(email)
      .clientId(clientId)
      .password(password)
      .secretHash(calculateHash(email))
      .build()
  }

  private[this] def calculateHash(username: String): String = {
    val mac = Mac.getInstance(HMAC_SHA256_ALGORITHM)
    mac.init(
      new SecretKeySpec(
        secretKey.getBytes(StandardCharsets.UTF_8),
        HMAC_SHA256_ALGORITHM
      )
    )

    mac.update(username.getBytes(StandardCharsets.UTF_8))
    val rawHmac = mac.doFinal(clientId.getBytes(StandardCharsets.UTF_8))
    java.util.Base64.getEncoder.encodeToString(rawHmac)
  }

  private[this] def performCall[T](
      call: () => T,
      requestType: CognitoRequestType,
      errorMessage: Throwable => String
  ): Future[CircuitBreakerResult[T]] = {
    val timer = metrics.reportCognitoRequestStarted(requestType)
    breaker.withBreaker(e => {
      metrics
        .reportCognitoFailure(timer, requestType)
      logger.error(errorMessage.apply(e), e)
    }) {
      Future {
        val result = call.apply
        metrics.reportCognitoRequestFinished(timer)
        result
      }
    }

  }
}
