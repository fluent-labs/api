name := "foreign-language-reader-parent"
organization := "com.foreignlanguagereader"
version := "1.0-SNAPSHOT"

scalaVersion in ThisBuild := "2.12.12"

lazy val global = project
  .in(file("."))
  .settings(settings)
  .disablePlugins(AssemblyPlugin)
  .aggregate(api, domain, dto, definitions)

lazy val dto = project
  .settings(
    settings,
    assemblySettings,
    libraryDependencies ++= commonDependencies
  )

lazy val domain = project
  .settings(
    settings,
    assemblySettings,
    libraryDependencies ++= commonDependencies ++ Seq(
      dependencies.utilBackports,
      // Used to generate elasticsearch matchers
      dependencies.elastic4s,
      dependencies.elastic4sPlay,
      dependencies.scalatestPlay,
      dependencies.opencc4j
    )
  )
  .dependsOn(dto)

lazy val api = project
  .enablePlugins(PlayService, PlayLayoutPlugin)
  .settings(
    settings,
    assemblySettings,
    libraryDependencies ++= commonDependencies ++ playDependencies ++ Seq(
      dependencies.elastic4s,
      dependencies.elastic4sTestkit,
      dependencies.elastic4sPlay,
      dependencies.googleCloudClient
    )
  )
  .dependsOn(domain)

lazy val definitions = project
  .in(file("content/definitions"))
  .settings(
    name := "definitions",
    assemblySettings,
    libraryDependencies ++= commonDependencies ++ Seq(
      dependencies.sparkCore % "provided",
      dependencies.sparkSql % "provided",
      dependencies.sparkXml
    )
  )
  .dependsOn(domain)

lazy val commonDependencies = Seq(
  dependencies.scalatest % "test",
  dependencies.scalactic,
  dependencies.cats,
  ws,
  dependencies.sangria
)

lazy val playDependencies = Seq(
  dependencies.scalatestPlay,
  dependencies.mockito,
  guice,
  dependencies.sangria,
  dependencies.sangriaPlay
)

lazy val dependencies =
  new {
    val scalatestVersion = "3.2.2"
    val sparkVersion = "3.0.1"
    val sparkXmlVersion = "0.10.0"
    val elastic4sVersion = "7.8.0"

    val scalactic = "org.scalactic" %% "scalactic" % scalatestVersion
    val scalatest = "org.scalatest" %% "scalatest" % scalatestVersion
    val scalatestPlay =
      "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test
    val mockito = "org.mockito" %% "mockito-scala" % "1.16.0" % Test

    val cats = "org.typelevel" %% "cats-core" % "2.1.1"
    val utilBackports = "com.github.bigwheel" %% "util-backports" % "2.1"

    val sparkCore = "org.apache.spark" %% "spark-core" % sparkVersion
    val sparkSql = "org.apache.spark" %% "spark-sql" % sparkVersion
    val sparkXml = "com.databricks" %% "spark-xml" % sparkXmlVersion

    val elastic4s =
      "com.sksamuel.elastic4s" %% "elastic4s-client-esjava" % elastic4sVersion
    val elastic4sTestkit =
      "com.sksamuel.elastic4s" %% "elastic4s-testkit" % elastic4sVersion % "test"

    val sangria = "org.sangria-graphql" %% "sangria" % "2.0.0"
    val elastic4sPlay =
      "com.sksamuel.elastic4s" %% "elastic4s-json-play" % elastic4sVersion
    val sangriaPlay = "org.sangria-graphql" %% "sangria-play-json" % "2.0.1"

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
