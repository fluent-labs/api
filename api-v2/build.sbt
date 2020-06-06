name := """foreign-language-reader-api"""
organization := "com.foreignlanguagereader"

version := "1.0-SNAPSHOT"

lazy val root = (project in file("."))
  .enablePlugins(PlayService, PlayLayoutPlugin)

scalaVersion := "2.13.2"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test
libraryDependencies += ws

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.foreignlanguagereader.binders._"
