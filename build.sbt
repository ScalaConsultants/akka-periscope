val akkaVersion        = "2.6.4"
val akkaHttpVersion    = "10.1.11"
val scalaTestVersion   = "3.1.1"

val compilerOptions = Seq(
  "-deprecation",
  "-encoding",
  "UTF-8",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-unchecked",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Xfatal-warnings",
  "-Ywarn-unused:imports"
)

val root = (project in file(".")).settings(
  organization := "io.scalac",
  scalaVersion := "2.13.2",
  scalacOptions ++= compilerOptions,
  name := "akka-actor-tree",
  libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-actor"          % akkaVersion,
    "com.typesafe.akka" %% "akka-http"           % akkaHttpVersion,
    "org.scalatest"     %% "scalatest"           % scalaTestVersion % Test,
    "com.typesafe.akka" %% "akka-testkit"        % akkaVersion % Test,
    "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test,
    "com.typesafe.akka" %% "akka-http-testkit"   % akkaHttpVersion % Test
  )
)
