package com.foreignlanguagereader.domain.client.aws

import akka.actor.ActorSystem
import com.foreignlanguagereader.domain.client.circuitbreaker.{
  CircuitBreakerAttempt,
  CircuitBreakerResult,
  Circuitbreaker
}
import com.foreignlanguagereader.domain.metrics.MetricsReporter
import com.foreignlanguagereader.domain.metrics.label.CognitoRequestType
import com.foreignlanguagereader.domain.metrics.label.CognitoRequestType.CognitoRequestType
import play.api.Logger
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient
import software.amazon.awssdk.services.cognitoidentityprovider.model.{
  AttributeType,
  AuthFlowType,
  InitiateAuthRequest,
  InitiateAuthResponse,
  RespondToAuthChallengeRequest,
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

  def login(
      email: String,
      password: String
  ): Future[CircuitBreakerResult[RespondToAuthChallengeRequest]] = {
    val secretHash = calculateHash(email)
    initiateAuthRequest(email, secretHash).flatMap {
      case CircuitBreakerAttempt(initiateAuthResult) =>
        respondToAuthChallengeRequest(initiateAuthResult, password, secretHash)
    }
  }

  private[this] def initiateAuthRequest(
      email: String,
      secretHash: String
  ): Future[CircuitBreakerResult[InitiateAuthResponse]] =
    performCall(
      () =>
        identityProviderClient.initiateAuth(
          makeInitiateAuthRequest(email, secretHash)
        ),
      CognitoRequestType.INITIATE_AUTH_REQUEST,
      e => s"Failed to log in user $email: ${e.getMessage}"
    )

  private[this] def makeInitiateAuthRequest(
      email: String,
      secretHash: String
  ): InitiateAuthRequest = {
    val parameters =
      Map("SECRET_HASH" -> secretHash, "USERNAME" -> email).asJava
    InitiateAuthRequest
      .builder()
      .clientId(clientId)
      .authFlow(AuthFlowType.USER_SRP_AUTH)
      .authParameters(parameters)
      .build()
  }

  private[this] def respondToAuthChallengeRequest(
      initiateAuthResult: InitiateAuthResponse,
      password: String,
      secretHash: String
  ): Future[CircuitBreakerResult[RespondToAuthChallengeRequest]] = ???

  private[this] def makeRespondToAuthChallengeRequest(
      initiateAuthResult: InitiateAuthResponse,
      password: String,
      secretHash: String
  ): RespondToAuthChallengeRequest = ???
  def buildPasswordAuthenticationKey(
      challegeParameters: Map[String, String],
      password: String
  ): Option[Array[Byte]] =
    for (
      userId <- challegeParameters.get("USER_ID_FOR_SRP");
      srpB <- challegeParameters.get("SRP_B");
      b <- Some(new BigInteger(srpB, hexadecimalBase))
      if !b.mod(N).equals(BigInteger.ZERO);
      salt <- challegeParameters.get("SALT")
    )
      yield getPasswordAuthenticationKey(
        userId,
        password,
        b,
        new BigInteger(salt, hexadecimalBase)
      )

  def getPasswordAuthenticationKey(
      userId: String,
      userPassword: String,
      b: BigInteger,
      salt: BigInteger
  ): Array[Byte] = ???

  def getHmac(
      key: Array[Byte],
      challegeParameters: Map[String, String],
      dateString: String
  ): Option[Array[Byte]] = {
    for (
      userId <- challegeParameters.get("USER_ID_FOR_SRP");
      secretBlock <-
        challegeParameters.get("SECRET_BLOCK").map(raw => base64Decode(raw))
    )
      yield calculateHash(
        key,
        List[String](userPoolId, userId, secretBlock, dateString)
      )
  }

  def base64Decode(input: String): String
  def base64Encode(input: Array[Byte]): String

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
      .secretHash(calculateEmailHash(email))
      .build()
  }

  private[this] def calculateEmailHash(email: String): String = {
    java.util.Base64.getEncoder.encodeToString(
      calculateHash(
        secretKey.getBytes(StandardCharsets.UTF_8),
        List(email, clientId)
      )
    )
  }

  private[this] def calculateHash(
      secretKey: Array[Byte],
      inputs: List[String]
  ): Array[Byte] = {
    val mac = Mac.getInstance(HMAC_SHA256_ALGORITHM)
    mac.init(new SecretKeySpec(secretKey, HMAC_SHA256_ALGORITHM))

    // All but last item
    inputs.init.foreach(input =>
      mac.update(input.getBytes(StandardCharsets.UTF_8))
    )
    mac.doFinal(inputs.last.getBytes(StandardCharsets.UTF_8))
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
