// Quality
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.6.1")
addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.10.0-RC1")

// Make fat jars for Spark jobs
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.15.0")

// Publishing
addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.13")
addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "1.0.0")
addSbtPlugin("com.codecommit" % "sbt-github-packages" % "0.5.2")

// Api
// Workaround for missing npm sources
addSbtPlugin(
  "com.typesafe.play" % "sbt-plugin" % "2.8.7" exclude ("org.webjars", "npm")
)
libraryDependencies += "org.webjars" % "npm" % "4.4.4"
