import play.sbt.PlayImport.{guice, ws}
import sbt._

/*
 * Dependencies
 */

object Dependencies {
  val elasticsearchVersion = "7.14.2"
  val hadoopVersion = "3.3.1"
  val jacksonVersion = "2.11.3"
  val log4jVersion = "2.17.1"
  val playSlickVersion = "5.0.0"
  val prometheusVersion = "0.14.1"
  val scalatestVersion = "3.2.10"
  val sparkVersion = "3.1.2"

  val content = "io.fluentlabs" %% "content" % "1.0.16"

  // Testing
  val scalactic = "org.scalactic" %% "scalactic" % scalatestVersion
  val scalatest = "org.scalatest" %% "scalatest" % scalatestVersion
  val scalatestPlay =
    "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test
  val mockito = "org.mockito" %% "mockito-scala" % "1.16.55" % Test
  val elasticsearchContainer =
    "org.testcontainers" % "elasticsearch" % "1.16.3"

  // Language helpers
  val cats = "org.typelevel" %% "cats-core" % "2.7.0"
  val lombok = "org.projectlombok" % "lombok" % "1.18.16"

  // Logging
  val log4jImplementation =
    "org.apache.logging.log4j" % "log4j-slf4j-impl" % log4jVersion
  val log4jApi = "org.apache.logging.log4j" % "log4j-api" % log4jVersion
  val log4jCore = "org.apache.logging.log4j" % "log4j-core" % log4jVersion
  val log4jJson =
    "org.apache.logging.log4j" % "log4j-layout-template-json" % log4jVersion

  // Metrics
  val prometheusClient = "io.prometheus" % "simpleclient" % prometheusVersion
  val prometheusCommon =
    "io.prometheus" % "simpleclient_common" % prometheusVersion
  val prometheusHotspot =
    "io.prometheus" % "simpleclient_hotspot" % prometheusVersion

  // NLP tools
  val opencc4j = "com.github.houbb" % "opencc4j" % "1.7.2"

  // External clients
  val elasticsearchHighLevelClient =
    "org.elasticsearch.client" % "elasticsearch-rest-high-level-client" % elasticsearchVersion
  val oslib = "com.lihaoyi" %% "os-lib" % "0.8.0"
  val googleCloudClient =
    "com.google.cloud" % "google-cloud-language" % "2.1.5"

  // Database
  val h2 = "com.h2database" % "h2" % "2.1.210"
  val postgres = "org.postgresql" % "postgresql" % "42.3.1"
  val playSlick = "com.typesafe.play" %% "play-slick" % playSlickVersion
  val playSlickEvolutions =
    "com.typesafe.play" %% "play-slick-evolutions" % playSlickVersion

  // Auth
  val jwtPlay = "com.pauldijou" %% "jwt-play" % "5.0.0"
  val jwtCore = "com.pauldijou" %% "jwt-core" % "5.0.0"
  val jwksRsa = "com.auth0" % "jwks-rsa" % "0.20.1"

  // Hacks for guava incompatibility
  val hadoopMapreduceClient =
    "org.apache.hadoop" % "hadoop-mapreduce-client-core" % "2.7.2"

  // Security related dependency upgrades below here
  val jacksonScala =
    "com.fasterxml.jackson.module" %% "jackson-module-scala" % jacksonVersion
  val jacksonDatabind =
    "com.fasterxml.jackson.core" % "jackson-databind" % jacksonVersion
  val jacksonCore =
    "com.fasterxml.jackson.core" % "jackson-core" % jacksonVersion
  val commonsCompress = "org.apache.commons" % "commons-compress" % "1.21"

  // Do these still apply without spark?
  val htrace = "org.apache.htrace" % "htrace-core" % "4.0.0-incubating"
  val avro = "org.apache.avro" % "avro" % "1.10.0"
}

object ProjectDependencies {

  val commonDependencies = Seq(
    Dependencies.scalatest % "test",
    Dependencies.scalactic,
    Dependencies.cats,
    ws
  )

  val log4jDependencies = Seq(
    Dependencies.log4jApi,
    Dependencies.log4jCore,
    Dependencies.log4jImplementation,
    Dependencies.log4jJson
  )

  val playDependencies = Seq(
    Dependencies.scalatestPlay,
    Dependencies.mockito
  )

  val forcedDependencies = Seq(
    Dependencies.hadoopMapreduceClient,
    Dependencies.jacksonScala,
    Dependencies.jacksonDatabind,
    Dependencies.jacksonCore,
    Dependencies.lombok,
    Dependencies.htrace,
    Dependencies.avro,
    Dependencies.log4jApi,
    Dependencies.log4jCore,
    Dependencies.log4jImplementation,
    Dependencies.log4jJson,
    Dependencies.commonsCompress
  )

  val apiDependencies: Seq[ModuleID] =
    commonDependencies ++ playDependencies ++ log4jDependencies ++ Seq(
      Dependencies.prometheusClient,
      Dependencies.prometheusHotspot,
      Dependencies.prometheusCommon,
      Dependencies.h2,
      Dependencies.postgres
    )

  val domainDependencies: Seq[ModuleID] =
    commonDependencies ++ Seq(
      Dependencies.content,
      // Dependency injection
      guice,
      // Used to generate elasticsearch matchers
      Dependencies.elasticsearchHighLevelClient,
      Dependencies.oslib,
      // Testing
      Dependencies.mockito,
      Dependencies.scalatestPlay,
      Dependencies.elasticsearchContainer,
      // Clients
      Dependencies.opencc4j,
      Dependencies.googleCloudClient,
      // Metrics
      Dependencies.prometheusClient,
      Dependencies.prometheusHotspot,
      Dependencies.playSlick,
      // Auth
      Dependencies.jwtCore,
      Dependencies.jwtPlay,
      Dependencies.jwksRsa
    )

  val allDependencies: Seq[ModuleID] =
    apiDependencies ++ domainDependencies
}
