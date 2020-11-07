name := "foreign-language-reader-parent"
organization := "com.foreignlanguagereader"
version := "1.0-SNAPSHOT"

scalaVersion in ThisBuild := "2.11.12"

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
      // Used to generate elasticsearch matchers
      dependencies.elastic4s,
      dependencies.elastic4sPlay,
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
      dependencies.elastic4s,
      dependencies.elastic4sTestkit,
      dependencies.elastic4sPlay,
      // Testing
      dependencies.mockito,
      dependencies.scalatestPlay,
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
    dependencyOverrides += dependencies.hadoopClient
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
    )
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

lazy val dependencies =
  new {
    val scalatestVersion = "3.2.2"
    val sparkVersion = "2.4.4"
    val sparkXmlVersion = "0.10.0"
    val elastic4sVersion = "7.1.0"

    val scalactic = "org.scalactic" %% "scalactic" % scalatestVersion
    val scalatest = "org.scalatest" %% "scalatest" % scalatestVersion
    val scalatestPlay =
      "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.0" % Test
    val mockito = "org.mockito" %% "mockito-scala" % "1.16.0" % Test

    val cats = "org.typelevel" %% "cats-core" % "2.0.0"

    val sparkCore = "org.apache.spark" %% "spark-core" % sparkVersion
    val sparkSql = "org.apache.spark" %% "spark-sql" % sparkVersion
    val sparkMl =
      "org.apache.spark" %% "spark-mllib" % sparkVersion
    val sparkXml = "com.databricks" %% "spark-xml" % sparkXmlVersion
    val sparkNLP =
      "com.johnsnowlabs.nlp" %% "spark-nlp" % "2.6.3"

    // Hacks for guava incompatibility
    val hadoopClient =
      "org.apache.hadoop" % "hadoop-mapreduce-client-core" % "2.7.2"
    val hadoopCommon =
      "org.apache.hadoop" % "hadoop-common" % "2.7.2" // required for org.apache.hadoop.util.StopWatch
    val apacheCommonsIo =
      "commons-io" % "commons-io" % "2.4" // required for org.apache.commons.io.Charsets that is used internally

    val elastic4s =
      "com.sksamuel.elastic4s" %% "elastic4s-client-esjava" % elastic4sVersion
    val elastic4sTestkit =
      "com.sksamuel.elastic4s" %% "elastic4s-testkit" % elastic4sVersion % "test"

    val sangria = "org.sangria-graphql" %% "sangria" % "2.0.0"
    val elastic4sPlay =
      "com.sksamuel.elastic4s" %% "elastic4s-json-play" % elastic4sVersion
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
