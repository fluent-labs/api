name := "foreign-language-reader-parent"
organization := "com.foreignlanguagereader"
version := "1.0-SNAPSHOT"

scalaVersion in ThisBuild := "2.12.12"

lazy val global = project
  .in(file("."))
  .settings(settings)
  .disablePlugins(AssemblyPlugin)
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

lazy val dependencies =
  new {
    val scalatestVersion = "3.2.2"
    val sparkVersion = "3.0.1"
    val sparkXmlVersion = "0.10.0"
    val scalactic = "org.scalactic" %% "scalactic" % scalatestVersion
    val scalatest = "org.scalatest" %% "scalatest" % scalatestVersion
    val sparkCore = "org.apache.spark" %% "spark-core" % sparkVersion
    val sparkSql = "org.apache.spark" %% "spark-sql" % sparkVersion
    val sparkXml = "com.databricks" %% "spark-xml" % sparkXmlVersion
  }

lazy val assemblySettings = Seq(
  assemblyJarName in assembly := name.value + ".jar"
)

// Code coverage settings
coverageMinimum := 70
coverageFailOnMinimum := false
