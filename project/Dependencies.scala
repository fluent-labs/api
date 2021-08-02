import play.sbt.PlayImport.{guice, ws}
import sbt._

/*
 * Dependencies
 */

object Dependencies {
  val elasticsearchVersion = "7.10.1"
  val hadoopVersion = "2.7.4"
  val jacksonVersion = "2.11.3"
  val log4jVersion = "2.14.0"
  val playSlickVersion = "5.0.0"
  val prometheusVersion = "0.9.0"
  val scalatestVersion = "3.2.9"
  val sparkVersion = "3.0.1"

  // Testing
  val scalactic = "org.scalactic" %% "scalactic" % scalatestVersion
  val scalatest = "org.scalatest" %% "scalatest" % scalatestVersion
  val scalatestPlay =
    "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test
  val mockito = "org.mockito" %% "mockito-scala" % "1.16.0" % Test
  val elasticsearchContainer =
    "org.testcontainers" % "elasticsearch" % "1.15.0"

  // Language helpers
  val cats = "org.typelevel" %% "cats-core" % "2.0.0"
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

  // Content batch jobs
  val sparkCore =
    "org.apache.spark" %% "spark-core" % sparkVersion
  val sparkSql = "org.apache.spark" %% "spark-sql" % sparkVersion
  val sparkXml = "com.databricks" %% "spark-xml" % "0.10.0"
  val hadoop = "org.apache.hadoop" % "hadoop-common" % hadoopVersion
  val hadoopClient = "org.apache.hadoop" % "hadoop-client" % hadoopVersion
  val hadoopAWS = "org.apache.hadoop" % "hadoop-aws" % hadoopVersion
  val awsJavaSDK = "com.amazonaws" % "aws-java-sdk" % "1.7.4"
  // Enable this when it is build for scala 2.12 in main
  // And remove our local version
  //    val elasticsearchHadoop =
  //      "org.elasticsearch" % "elasticsearch-hadoop" % elasticsearchVersion

  // NLP tools
  val opencc4j = "com.github.houbb" % "opencc4j" % "1.6.0"

  // External clients
  val elasticsearchHighLevelClient =
    "org.elasticsearch.client" % "elasticsearch-rest-high-level-client" % elasticsearchVersion
  val oslib = "com.lihaoyi" %% "os-lib" % "0.7.1"
  val googleCloudClient =
    "com.google.cloud" % "google-cloud-language" % "1.103.2"

  // Database
  val h2 = "com.h2database" % "h2" % "1.4.192"
  val postgres = "org.postgresql" % "postgresql" % "42.2.18"
  val playSlick = "com.typesafe.play" %% "play-slick" % playSlickVersion
  val playSlickEvolutions =
    "com.typesafe.play" %% "play-slick-evolutions" % playSlickVersion

  // Auth
  val jwtPlay = "com.pauldijou" %% "jwt-play" % "4.3.0"
  val jwtCore = "com.pauldijou" %% "jwt-core" % "4.3.0"
  val jwksRsa = "com.auth0" % "jwks-rsa" % "0.15.0"

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
    Dependencies.hadoop,
    Dependencies.avro,
    Dependencies.log4jApi,
    Dependencies.log4jCore,
    Dependencies.log4jImplementation,
    Dependencies.log4jJson
  )

  val apiDependencies: Seq[ModuleID] =
    commonDependencies ++ playDependencies ++ log4jDependencies ++ Seq(
      Dependencies.prometheusClient,
      Dependencies.prometheusHotspot,
      Dependencies.prometheusCommon,
      Dependencies.h2,
      Dependencies.postgres
    )

  val contentDependencies: Seq[ModuleID] = commonDependencies ++ Seq(
    Dependencies.scalatestPlay,
    Dependencies.opencc4j
  )

  val domainDependencies: Seq[ModuleID] =
    commonDependencies ++ Seq(
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

  val dtoDependencies: Seq[ModuleID] = commonDependencies

  val jobsDependencies: Seq[ModuleID] =
    commonDependencies ++ log4jDependencies ++ Seq(
      Dependencies.sparkCore % "provided",
      Dependencies.sparkSql % "provided",
      Dependencies.sparkXml,
      // S3 support
      Dependencies.hadoop,
      Dependencies.hadoopClient,
      Dependencies.hadoopAWS,
      Dependencies.awsJavaSDK
    )

  val allDependencies: Seq[ModuleID] =
    apiDependencies ++ contentDependencies ++ domainDependencies ++ dtoDependencies ++ jobsDependencies
}
