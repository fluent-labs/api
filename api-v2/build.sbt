name := """foreign-language-reader-api"""
organization := "com.foreignlanguagereader"

version := "1.0-SNAPSHOT"

lazy val root = (project in file("."))
  .enablePlugins(PlayService, PlayLayoutPlugin)

scalaVersion := "2.13.2"

// Give deprecation and feature warnings on compile
scalacOptions ++= Seq("-deprecation", "-feature")

// Dependency injection
libraryDependencies += guice

// Testing
val scalatestVersion = "3.0.8"
libraryDependencies += "org.scalactic" %% "scalactic" % scalatestVersion
libraryDependencies += "org.scalatest" %% "scalatest" % scalatestVersion % "test"
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test

// REST calls
libraryDependencies += ws

libraryDependencies += "com.google.cloud" % "google-cloud-language" % "1.100.0"

val elastic4sVersion = "7.8.0"
libraryDependencies ++= Seq(
  "com.sksamuel.elastic4s" %% "elastic4s-client-esjava" % elastic4sVersion,
  "com.sksamuel.elastic4s" %% "elastic4s-testkit" % elastic4sVersion % "test"
)
libraryDependencies += "com.sksamuel.elastic4s" % "elastic4s-json-play_2.13" % elastic4sVersion

libraryDependencies += "org.sangria-graphql" %% "sangria" % "2.0.0"
libraryDependencies += "org.sangria-graphql" %% "sangria-play-json" % "2.0.1"
// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.foreignlanguagereader.binders._"

// Code coverage settings
coverageEnabled := true
coverageMinimum := 70
coverageFailOnMinimum := true
