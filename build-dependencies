import Dependencies._

name := "foreign-language-reader-parent"
scalaVersion in ThisBuild := "2.12.12"

lazy val dependencies = project
  .in(file("."))
  .settings(
    libraryDependencies ++= ProjectDependencies.allDependencies,
    dependencyOverrides ++= ProjectDependencies.forcedDependencies
  )
