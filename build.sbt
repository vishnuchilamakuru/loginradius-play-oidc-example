name := """loginradius-play-oidc-example"""
organization := "com.loginradius.example"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.13.6"

initialize := {
  val _ = initialize.value // run the previous initialization
  val required = "11"
  val current  = sys.props("java.specification.version")
  assert(current == required, s"Unsupported JDK: java.specification.version $current != $required")
}

scalacOptions += "-target:jvm-11"

javacOptions ++= Seq("-source", "11", "-target", "11")


libraryDependencies ++= Seq(
  guice,
  ehcache,
  "org.pac4j" %% "play-pac4j" % "11.0.0-PLAY2.8",
  "org.pac4j" % "pac4j-oidc" % "5.1.0",
  "com.typesafe.play" % "play-cache_2.13" % "2.8.8",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.12.3"
)