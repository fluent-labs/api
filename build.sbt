import sbtassembly.AssemblyPlugin.autoImport.assemblyMergeStrategy

name := "foreign-language-reader-parent"
scalaVersion in ThisBuild := "2.12.12"

/*
 * Project Setup
 */

lazy val settings = Seq(
  scalacOptions ++= compilerOptions,
  githubTokenSource := TokenSource.Or(
    TokenSource.Environment("GITHUB_TOKEN"),
    TokenSource.GitConfig("github.token")
  ),
  releaseVersionBump := sbtrelease.Version.Bump.Bugfix,
  publishConfiguration := publishConfiguration.value.withOverwrite(true),
  publishLocalConfiguration := publishLocalConfiguration.value.withOverwrite(
    true
  )
)

lazy val global = project
  .in(file("."))
  .disablePlugins(AssemblyPlugin)
  .settings(
    settings,
    assemblySettings,
    dependencyOverrides ++= forcedDependencies
  )
  .aggregate(api, content, domain, dto, jobs)

lazy val api = project
  .enablePlugins(PlayService, PlayLayoutPlugin)
  .disablePlugins(PlayLogback)
  .settings(
    settings,
    assemblySettings,
    libraryDependencies ++= apiDependencies,
    dependencyOverrides ++= forcedDependencies,
    javaOptions += "-Dlog4j.configurationFile=log4j2.xml"
  )
  .dependsOn(domain)

lazy val content = project
  .settings(
    settings,
    assemblySettings,
    libraryDependencies ++= contentDependencies,
    dependencyOverrides ++= forcedDependencies
  )
  .dependsOn(dto)

lazy val domain = project
  .settings(
    settings,
    assemblySettings,
    libraryDependencies ++= domainDependencies,
    dependencyOverrides ++= forcedDependencies
  )
  .dependsOn(content)

lazy val dto = project
  .settings(
    settings,
    assemblySettings,
    libraryDependencies ++= dtoDependencies,
    dependencyOverrides ++= forcedDependencies
  )

lazy val jobs = project
  .enablePlugins(AssemblyPlugin)
  .settings(
    settings,
    assemblySettings,
    libraryDependencies ++= jobsDependencies,
    dependencyOverrides ++= forcedDependencies
  )
  .dependsOn(content)

/*
 * Dependencies
 */

lazy val dependencies =
  new {
    val scalatestVersion = "3.2.2"
    val jacksonVersion = "2.11.3"
    val log4jVersion = "2.14.0"
    val elasticsearchVersion = "7.10.1"
    val sparkVersion = "3.0.1"
    val hadoopVersion = "2.7.4"
    val prometheusVersion = "0.9.0"

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

    val log4jImplementation =
      "org.apache.logging.log4j" % "log4j-slf4j-impl" % log4jVersion
    val log4jApi = "org.apache.logging.log4j" % "log4j-api" % log4jVersion
    val log4jCore = "org.apache.logging.log4j" % "log4j-core" % log4jVersion
    val log4jJson =
      "org.apache.logging.log4j" % "log4j-layout-template-json" % log4jVersion

    val prometheusClient = "io.prometheus" % "simpleclient" % prometheusVersion
    val prometheusHotspot =
      "io.prometheus" % "simpleclient_hotspot" % prometheusVersion

    // Spark
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
      "com.google.cloud" % "google-cloud-language" % "1.101.6"

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

lazy val commonDependencies = Seq(
  dependencies.scalatest % "test",
  dependencies.scalactic,
  dependencies.cats,
  ws
)

lazy val log4jDependencies = Seq(
  dependencies.log4jApi,
  dependencies.log4jCore,
  dependencies.log4jImplementation,
  dependencies.log4jJson
)

lazy val playDependencies = Seq(
  dependencies.scalatestPlay,
  dependencies.mockito
)

lazy val forcedDependencies = Seq(
  dependencies.hadoopMapreduceClient,
  dependencies.jacksonScala,
  dependencies.jacksonDatabind,
  dependencies.jacksonCore,
  dependencies.lombok,
  dependencies.htrace,
  dependencies.hadoop,
  dependencies.avro,
  dependencies.log4jApi,
  dependencies.log4jCore,
  dependencies.log4jImplementation,
  dependencies.log4jJson
)

lazy val apiDependencies =
  commonDependencies ++ playDependencies ++ log4jDependencies ++ Seq(
    dependencies.prometheusClient,
    dependencies.prometheusHotspot
  )

lazy val contentDependencies = commonDependencies ++ Seq(
  dependencies.scalatestPlay,
  dependencies.opencc4j
)

lazy val domainDependencies = commonDependencies ++ Seq(
  // Dependency injection
  guice,
  // Used to generate elasticsearch matchers
  dependencies.elasticsearchHighLevelClient,
  dependencies.oslib,
  // Testing
  dependencies.mockito,
  dependencies.scalatestPlay,
  dependencies.elasticsearchContainer,
  // Clients
  dependencies.opencc4j,
  dependencies.googleCloudClient
)

lazy val dtoDependencies = commonDependencies

lazy val jobsDependencies = commonDependencies ++ log4jDependencies ++ Seq(
  dependencies.sparkCore % "provided",
  dependencies.sparkSql % "provided",
  dependencies.sparkXml,
  // S3 support
  dependencies.hadoop,
  dependencies.hadoopClient,
  dependencies.hadoopAWS,
  dependencies.awsJavaSDK
)

/*
 * Build
 */

lazy val compilerOptions = Seq(
  "-encoding",
  "utf8",
  "-deprecation",
  "-feature",
  "-unchecked",
  "-Xfatal-warnings",
  "-Ypartial-unification" // Remove me in scala 2.13
)
// Add these back in when we can get to scala 2.13
//  "-Wdead-code",
//  "-Wvalue-discard",

/*
 * Release
 */

lazy val assemblySettings = Seq(
  organization := "com.foreignlanguagereader",
  githubOwner := "foreign-language-reader",
  githubRepository := "api",
  // Used for building jobs fat jars
  assemblyJarName in assembly := name.value + ".jar",
  assemblyMergeStrategy in assembly := {
    case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
    case _                                   => MergeStrategy.first
  }
)

/*
 * Quality
 */

// Code coverage settings
coverageMinimum := 70
coverageFailOnMinimum := false
