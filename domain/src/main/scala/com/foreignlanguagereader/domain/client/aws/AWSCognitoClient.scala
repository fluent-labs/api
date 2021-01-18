package com.foreignlanguagereader.domain.client.aws

import akka.actor.ActorSystem
import cats.data.Nested
import cats.implicits._
import com.foreignlanguagereader.domain.client.circuitbreaker.{
  CircuitBreakerAttempt,
  CircuitBreakerFailedAttempt,
  CircuitBreakerNonAttempt,
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
  RespondToAuthChallengeResponse,
  SignUpRequest,
  SignUpResponse
}

import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.{Date, Locale, SimpleTimeZone}
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
  val userPoolId: String =
    "something_else".split("_", 2)(1)

  val hexadecimalBase = 16
  val HMAC_SHA256_ALGORITHM = "HmacSHA256"
  val HEX_N: String =
    "FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD1" +
      "29024E088A67CC74020BBEA63B139B22514A08798E3404DD" +
      "EF9519B3CD3A431B302B0A6DF25F14374FE1356D6D51C245" +
      "E485B576625E7EC6F44C42E9A637ED6B0BFF5CB6F406B7ED" +
      "EE386BFB5A899FA5AE9F24117C4B1FE649286651ECE45B3D" +
      "C2007CB8A163BF0598DA48361C55D39A69163FA8FD24CF5F" +
      "83655D23DCA3AD961C62F356208552BB9ED529077096966D" +
      "670C354E4ABC9804F1746C08CA18217C32905E462E36CE3B" +
      "E39E772C180E86039B2783A2EC07A28FB5C55DF06F4C52C9" +
      "DE2BCBF6955817183995497CEA956AE515D2261898FA0510" +
      "15728E5A8AAAC42DAD33170D04507A33A85521ABDF1CBA64" +
      "ECFB850458DBEF0A8AEA71575D060C7DB3970F85A6E1E4C7" +
      "ABF5AE8CDB0933D71E8C94E04A25619DCEE3D2261AD2EE6B" +
      "F12FFA06D98A0864D87602733EC86A64521F2B18177B200C" +
      "BBE117577A615D6C770988C0BAD946E208E24FA074E5AB31" +
      "43DB5BFCE0FD108E4B82D120A93AD2CAFFFFFFFFFFFFFFFF"
  val N = new BigInteger(HEX_N, hexadecimalBase)
  val dateFormat: SimpleDateFormat = {
    val simpleDateFormat =
      new SimpleDateFormat("EEE MMM d HH:mm:ss z yyyy", Locale.US)
    simpleDateFormat.setTimeZone(
      new SimpleTimeZone(SimpleTimeZone.UTC_TIME, "UTC")
    )
    simpleDateFormat
  }

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
  ): Future[CircuitBreakerResult[String]] = {
    val secretHash = calculateEmailHash(email)
    val response: Future[CircuitBreakerResult[RespondToAuthChallengeResponse]] =
      initiateAuthRequest(email, secretHash).flatMap {
        case CircuitBreakerAttempt(initiateAuthResult) =>
          respondToAuthChallengeRequest(
            initiateAuthResult,
            email,
            password,
            secretHash
          )
        case CircuitBreakerFailedAttempt(e) =>
          Future.successful(CircuitBreakerFailedAttempt(e))
        case CircuitBreakerNonAttempt() =>
          Future.successful(CircuitBreakerNonAttempt())
      }
    Nested(response).map(_.authenticationResult().idToken()).value
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
      challenge: InitiateAuthResponse,
      username: String,
      password: String,
      secretHash: String
  ): Future[CircuitBreakerResult[RespondToAuthChallengeResponse]] =
    performCall(
      () =>
        identityProviderClient.respondToAuthChallenge(
          makeRespondToAuthChallengeRequest(
            challenge,
            username,
            password,
            secretHash
          )
        ),
      CognitoRequestType.RESPOND_TO_AUTH_CHALLENGE,
      e => s"Failed to log in user $username: ${e.getMessage}"
    )

  private[this] def makeRespondToAuthChallengeRequest(
      challenge: InitiateAuthResponse,
      username: String,
      password: String,
      secretHash: String
  ): RespondToAuthChallengeRequest = {
    val challengeParameters = challenge.challengeParameters().asScala.toMap
    val timestamp = dateFormat.format(new Date())

    val authResponses =
      for (
        key <- buildPasswordAuthenticationKey(challengeParameters, password);
        hmac <- getHmac(key, challengeParameters, timestamp);
        secretBlock <- challengeParameters.get("SECRET_BLOCK")
      )
        yield Map(
          "PASSWORD_CLAIM_SECRET_BLOCK" -> secretBlock,
          "PASSWORD_CLAIM_SIGNATURE" -> base64Encode(hmac),
          "TIMESTAMP" -> timestamp,
          "USERNAME" -> username,
          "SECRET_HASH" -> secretHash
        )

    RespondToAuthChallengeRequest
      .builder()
      .challengeName(challenge.challengeName())
      .clientId(clientId)
      .session(challenge.session())
      .challengeResponses(authResponses.getOrElse(Map()).asJava)
      .build()
  }

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
