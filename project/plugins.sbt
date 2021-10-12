// Quality
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.9.1")
addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.10.0-RC1")

// Publishing
addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.13")
addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "1.0.1")
addSbtPlugin("com.codecommit" % "sbt-github-packages" % "0.5.3")

// Api
// Workaround for missing npm sources
addSbtPlugin(
  "com.typesafe.play" % "sbt-plugin" % "2.8.8" exclude ("org.webjars", "npm")
)
libraryDependencies += "org.webjars" % "npm" % "5.0.0-2"
