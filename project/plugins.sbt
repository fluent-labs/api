// Quality
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "2.0.0-M4")
addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.10.0-RC1")

// Publishing
addSbtPlugin("com.github.sbt" % "sbt-release" % "1.1.0")
addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "1.0.2")
addSbtPlugin("com.codecommit" % "sbt-github-packages" % "0.5.3")

// Api
// Workaround for missing npm sources
addSbtPlugin(
  "com.typesafe.play" % "sbt-plugin" % "2.8.8" exclude ("org.webjars", "npm")
)
libraryDependencies += "org.webjars" % "npm" % "5.0.0-2"
