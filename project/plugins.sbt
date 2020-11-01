// Shared
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.6.1")
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.15.0")

// Api
// Workaround for missing npm sources
addSbtPlugin(
  "com.typesafe.play" % "sbt-plugin" % "2.8.0" exclude ("org.webjars", "npm")
)
libraryDependencies += "org.webjars" % "npm" % "4.2.0"
