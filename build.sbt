name := "foreign-language-reader-parent"
organization := "com.foreignlanguagereader"
version := "1.0-SNAPSHOT"

scalaVersion in ThisBuild := "2.11.12"

lazy val global = project
  .in(file("."))
  .settings(
    settings
  )
  .disablePlugins(AssemblyPlugin)
  .aggregate(api, content, domain, dto, jobs)

lazy val api = project
  .enablePlugins(PlayService, PlayLayoutPlugin)
  .settings(
    settings,
    assemblySettings,
    libraryDependencies ++= commonDependencies ++ playDependencies,
    dependencyOverrides ++= forcedDependencies
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
      // Spark NLP
      dependencies.sparkCore,
      dependencies.sparkSql,
      dependencies.sparkNLP,
      dependencies.sparkMl,
      // Handles breaking guava changes https://stackoverflow.com/questions/36427291/illegalaccesserror-to-guavas-stopwatch-from-org-apache-hadoop-mapreduce-lib-inp
      dependencies.hadoopCommon,
      dependencies.apacheCommonsIo
    ),
    dependencyOverrides ++= forcedDependencies
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

// Pretty much everything in here is because Spark NLP has versions that conflicts with other dependencies.
lazy val forcedDependencies = Seq(
  dependencies.hadoopClient,
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.6.7.1",
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.6.7",
  "com.fasterxml.jackson.core" % "jackson-core" % "2.6.7",
  "org.projectlombok" % "lombok" % "1.18.16"
)

lazy val dependencies =
  new {
    val scalatestVersion = "3.2.2"
    val sparkVersion = "2.4.4"

    val scalactic = "org.scalactic" %% "scalactic" % scalatestVersion
    val scalatest = "org.scalatest" %% "scalatest" % scalatestVersion
    val scalatestPlay =
      "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.0" % Test
    val mockito = "org.mockito" %% "mockito-scala" % "1.16.0" % Test
    val elasticsearchContainer =
      "org.testcontainers" % "elasticsearch" % "1.15.0"

    val cats = "org.typelevel" %% "cats-core" % "2.0.0"

    val sparkCore = "org.apache.spark" %% "spark-core" % sparkVersion
    val sparkSql = "org.apache.spark" %% "spark-sql" % sparkVersion
    val sparkMl =
      "org.apache.spark" %% "spark-mllib" % sparkVersion
    val sparkXml = "com.databricks" %% "spark-xml" % "0.10.0"
    val sparkNLP =
      "com.johnsnowlabs.nlp" %% "spark-nlp" % "2.6.3"

    // Hacks for guava incompatibility
    val hadoopClient =
      "org.apache.hadoop" % "hadoop-mapreduce-client-core" % "2.7.2"
    val hadoopCommon =
      "org.apache.hadoop" % "hadoop-common" % "2.7.2" // required for org.apache.hadoop.util.StopWatch
    val apacheCommonsIo =
      "commons-io" % "commons-io" % "2.4" // required for org.apache.commons.io.Charsets that is used internally

    val elasticsearchHighLevelClient =
      "org.elasticsearch.client" % "elasticsearch-rest-high-level-client" % "7.9.3"

    val sangria = "org.sangria-graphql" %% "sangria" % "2.0.0"
    val sangriaPlay = "org.sangria-graphql" %% "sangria-play-json" % "2.0.0"

    val googleCloudClient =
      "com.google.cloud" % "google-cloud-language" % "1.100.0"

    // Chinese language processing untilities.
    val opencc4j = "com.github.houbb" % "opencc4j" % "1.6.0"
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
