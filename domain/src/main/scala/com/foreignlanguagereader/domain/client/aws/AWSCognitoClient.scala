package com.foreignlanguagereader.domain.client.aws

import akka.actor.ActorSystem
import com.foreignlanguagereader.domain.client.common.Circuitbreaker
import com.foreignlanguagereader.domain.metrics.MetricsReporter
import play.api.Logger
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class AWSCognitoClient @Inject() (
    metrics: MetricsReporter,
    implicit val ec: ExecutionContext,
    val system: ActorSystem
) {
  val identityProviderClient: CognitoIdentityProviderClient =
    CognitoIdentityProviderClient
      .builder()
      .region(Region.US_WEST_2)
      .build()

  val logger: Logger = Logger(this.getClass)
  val breaker: Circuitbreaker = new Circuitbreaker(system, ec, "Cognito")
}
