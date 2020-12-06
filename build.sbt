name := "foreign-language-reader-parent"
organization := "com.foreignlanguagereader"
version := "1.0-SNAPSHOT"

scalaVersion in ThisBuild := "2.12.12"

lazy val global = project
  .in(file("."))
  .settings(settings)
  .disablePlugins(AssemblyPlugin)
  .aggregate(api, content, domain, dto, jobs)

lazy val api = project
  .enablePlugins(PlayService, PlayLayoutPlugin)
  .settings(
    settings,
    assemblySettings,
    libraryDependencies ++= commonDependencies ++ playDependencies
  )
  .dependsOn(domain)

lazy val content = project
  .settings(
    settings,
    assemblySettings,
    libraryDependencies ++= commonDependencies ++ Seq(
      dependencies.scalatestPlay,
      dependencies.opencc4j
    )
  )
  .dependsOn(dto)

lazy val domain = project
  .settings(
    settings,
    assemblySettings,
    libraryDependencies ++= commonDependencies ++ Seq(
      // Dependency injection
      guice,
      // Used to generate elasticsearch matchers
      dependencies.elasticsearchHighLevelClient,
      // Testing
      dependencies.mockito,
      dependencies.scalatestPlay,
      dependencies.elasticsearchContainer,
      // Clients
      dependencies.opencc4j,
      dependencies.googleCloudClient,
      // NLP
      dependencies.coreNLP,
      dependencies.coreNLPModels,
      dependencies.coreNLPChinese,
      dependencies.coreNLPEnglish,
      dependencies.coreNLPSpanish
    )
  )
  .dependsOn(content)

lazy val dto = project
  .settings(
    settings,
    assemblySettings,
    libraryDependencies ++= commonDependencies
  )

lazy val jobs = project
  .settings(
    assemblySettings,
    libraryDependencies ++= commonDependencies ++ Seq(
      dependencies.sparkCore % "provided",
      dependencies.sparkSql % "provided",
      dependencies.sparkXml
    ),
    dependencyOverrides ++= forcedDependencies
  )
  .dependsOn(content)

lazy val commonDependencies = Seq(
  dependencies.scalatest % "test",
  dependencies.scalactic,
  dependencies.cats,
  ws,
  dependencies.sangria
)

lazy val playDependencies = Seq(
  dependencies.scalatestPlay,
  dependencies.sangria,
  dependencies.sangriaPlay,
  dependencies.mockito
)

lazy val forcedDependencies = Seq(
  dependencies.hadoopClient,
  dependencies.jacksonScala,
  dependencies.jacksonDatabind,
  dependencies.jacksonCore,
  dependencies.lombok,
  dependencies.htrace,
  dependencies.hadoop,
  dependencies.avro
)

lazy val dependencies =
  new {
    val scalatestVersion = "3.2.2"
    val sparkVersion = "3.0.1"
    val jacksonVersion = "2.11.3"
    val coreNLPVersion = "4.2.0"

    // Testing
    val scalactic = "org.scalactic" %% "scalactic" % scalatestVersion
    val scalatest = "org.scalatest" %% "scalatest" % scalatestVersion
    val scalatestPlay =
      "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test
    val mockito = "org.mockito" %% "mockito-scala" % "1.16.0" % Test
    val elasticsearchContainer =
      "org.testcontainers" % "elasticsearch" % "1.15.0"

    val cats = "org.typelevel" %% "cats-core" % "2.0.0"

    // Spark
    val sparkCore =
      "org.apache.spark" %% "spark-core" % sparkVersion
    val sparkSql = "org.apache.spark" %% "spark-sql" % sparkVersion
    val sparkXml = "com.databricks" %% "spark-xml" % "0.10.0"

    // NLP tools
    val opencc4j = "com.github.houbb" % "opencc4j" % "1.6.0"
    val coreNLP = "edu.stanford.nlp" % "stanford-corenlp" % coreNLPVersion
    val coreNLPModels =
      "edu.stanford.nlp" % "stanford-corenlp" % coreNLPVersion classifier "models"
    val coreNLPChinese =
      "edu.stanford.nlp" % "stanford-corenlp" % coreNLPVersion classifier "models-chinese"
    val coreNLPEnglish =
      "edu.stanford.nlp" % "stanford-corenlp" % coreNLPVersion classifier "models-english"
    val coreNLPSpanish =
      "edu.stanford.nlp" % "stanford-corenlp" % coreNLPVersion classifier "models-spanish"

    // Graphql
    val sangria = "org.sangria-graphql" %% "sangria" % "2.0.0"
    val sangriaPlay = "org.sangria-graphql" %% "sangria-play-json" % "2.0.0"

    // External clients
    val elasticsearchHighLevelClient =
      "org.elasticsearch.client" % "elasticsearch-rest-high-level-client" % "7.9.3"
    val googleCloudClient =
      "com.google.cloud" % "google-cloud-language" % "1.101.6"

    // Hacks for guava incompatibility
    val hadoopClient =
      "org.apache.hadoop" % "hadoop-mapreduce-client-core" % "2.7.2"

    // Security related dependency upgrades below here
    val jacksonScala =
      "com.fasterxml.jackson.module" %% "jackson-module-scala" % jacksonVersion
    val jacksonDatabind =
      "com.fasterxml.jackson.core" % "jackson-databind" % jacksonVersion
    val jacksonCore =
      "com.fasterxml.jackson.core" % "jackson-core" % jacksonVersion
    val lombok = "org.projectlombok" % "lombok" % "1.18.16"
    val htrace = "org.apache.htrace" % "htrace-core" % "4.0.0-incubating"
    val hadoop = "org.apache.hadoop" % "hadoop-common" % "2.10.1"
    val avro = "org.apache.avro" % "avro" % "1.10.0"
  }

lazy val settings = Seq(
  scalacOptions ++= compilerOptions
)

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

lazy val assemblySettings = Seq(
  assemblyJarName in assembly := name.value + ".jar"
)

// Code coverage settings
coverageMinimum := 70
coverageFailOnMinimum := false
