import play.sbt.routes.RoutesKeys
import Dependencies._

name := "fluentlabs-parent"
scalaVersion in ThisBuild := "2.13.7"

/*
 * Project Setup
 */

lazy val settings = Seq(
  scalacOptions ++= compilerOptions,
  // Github  packages
  organization := "io.fluentlabs",
  githubOwner := "fluent-labs",
  githubRepository := "api",
  githubTokenSource := TokenSource.Or(
    TokenSource.Environment("GITHUB_TOKEN"),
    TokenSource.GitConfig("github.token")
  ),
  // Making semver releases
  releaseVersionBump := sbtrelease.Version.Bump.Bugfix,
  publishConfiguration := publishConfiguration.value.withOverwrite(true),
  publishLocalConfiguration := publishLocalConfiguration.value.withOverwrite(
    true
  )
)

lazy val global = project
  .in(file("."))
  .settings(
    settings,
    dependencyOverrides ++= ProjectDependencies.forcedDependencies
  )
  .aggregate(api, domain)

lazy val api = project
  .enablePlugins(PlayService, PlayLayoutPlugin)
  .disablePlugins(PlayLogback)
  .settings(
    settings,
    libraryDependencies ++= ProjectDependencies.apiDependencies,
    dependencyOverrides ++= ProjectDependencies.forcedDependencies,
    javaOptions += "-Dlog4j.configurationFile=log4j2.xml",
    RoutesKeys.routesImport += "io.fluentlabs.api.controller.v1.PathBinders._"
  )
  .dependsOn(domain)

lazy val domain = project
  .settings(
    settings,
    libraryDependencies ++= ProjectDependencies.domainDependencies,
    dependencyOverrides ++= ProjectDependencies.forcedDependencies
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
 * Quality
 */

// Code coverage settings
coverageMinimumStmtTotal := 70
coverageFailOnMinimum := false
