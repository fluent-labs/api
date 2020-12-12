// Quality
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.6.1")
addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.10.0-RC1")

// Make fat jars for Spark jobs
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.15.0")

// Publishing
addSbtPlugin("com.codecommit" % "sbt-github-packages" % "0.5.2")
addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.13")

// Api
// Workaround for missing npm sources
addSbtPlugin(
  "com.typesafe.play" % "sbt-plugin" % "2.8.4" exclude ("org.webjars", "npm")
)
libraryDependencies += "org.webjars" % "npm" % "4.2.0"
