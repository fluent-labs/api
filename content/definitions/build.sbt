name := "definitions"

version := "0.1"

scalaVersion := "2.11.12"
val scalatestVersion = "3.0.8"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "2.4.0",
  "org.apache.spark" %% "spark-sql" % "2.4.0",
  "com.databricks" %% "spark-xml" % "0.10.0",
  "org.scalactic" %% "scalactic" % scalatestVersion,
  "org.scalatest" %% "scalatest" % scalatestVersion % "test"
)

// Code coverage settings
coverageEnabled := true
coverageMinimum := 70
coverageFailOnMinimum := false
