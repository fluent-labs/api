import sbtassembly.AssemblyPlugin.autoImport.assemblyMergeStrategy
import Dependencies._

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
    dependencyOverrides ++= ProjectDependencies.forcedDependencies
  )
  .aggregate(api, content, domain, dto, jobs)

lazy val api = project
  .enablePlugins(PlayService, PlayLayoutPlugin)
  .disablePlugins(PlayLogback)
  .settings(
    settings,
    assemblySettings,
    libraryDependencies ++= ProjectDependencies.apiDependencies,
    dependencyOverrides ++= ProjectDependencies.forcedDependencies,
    javaOptions += "-Dlog4j.configurationFile=log4j2.xml"
  )
  .dependsOn(domain)

lazy val content = project
  .settings(
    settings,
    assemblySettings,
    libraryDependencies ++= ProjectDependencies.contentDependencies,
    dependencyOverrides ++= ProjectDependencies.forcedDependencies
  )
  .dependsOn(dto)

lazy val domain = project
  .settings(
    settings,
    assemblySettings,
    libraryDependencies ++= ProjectDependencies.domainDependencies,
    dependencyOverrides ++= ProjectDependencies.forcedDependencies
  )
  .dependsOn(content)

lazy val dto = project
  .settings(
    settings,
    assemblySettings,
    libraryDependencies ++= ProjectDependencies.dtoDependencies,
    dependencyOverrides ++= ProjectDependencies.forcedDependencies
  )

lazy val jobs = project
  .enablePlugins(AssemblyPlugin)
  .settings(
    settings,
    assemblySettings,
    libraryDependencies ++= ProjectDependencies.jobsDependencies,
    dependencyOverrides ++= ProjectDependencies.forcedDependencies
  )
  .dependsOn(content)

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
