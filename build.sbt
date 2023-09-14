import ReleaseTransformations._
import ReleasePlugin.autoImport._

val akkaVersion      = "2.6.5"
val akkaHttpVersion  = "10.1.12"
val circeVersion     = "0.13.0"
val scalaTestVersion = "3.1.1"

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
  "-Xfuture",
  "-Xfatal-warnings",
  "-Ywarn-unused-import"
)

val buildSettings = Seq(
  organization := "io.scalac",
  scalaVersion := "2.13.12",
  crossScalaVersions := Seq("2.12.11", "2.13.12"),
  scalacOptions ++= {
    if (priorTo2_13(scalaVersion.value)) compilerOptions
    else
      compilerOptions.flatMap {
        case "-Ywarn-unused-import" => Seq("-Ywarn-unused:imports")
        case "-Xfuture"             => Nil
        case other                  => Seq(other)
      }
  }
)

val publishSettings = Seq(
  releaseUseGlobalVersion := true,
  releaseVersionFile := file(".") / "version.sbt",
  releaseCommitMessage := s"Set version to ${version.value}",
  releaseIgnoreUntrackedFiles := true,
  releaseCrossBuild := true,
  homepage := Some(url("https://github.com/ScalaConsultants/akka-periscope")),
  licenses := Seq("MIT" -> url("https://opensource.org/licenses/MIT")),
  publishTo := sonatypePublishToBundle.value,
  publishMavenStyle := true,
  publishArtifact in Test := false,
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/ScalaConsultants/akka-periscope"),
      "scm:git:git@github.com:ScalaConsultants/akka-periscope.git"
    )
  ),
  developers := List(
    Developer(
      id = "vpavkin",
      name = "Vladimir Pavkin",
      email = "vpavkin@gmail.com",
      url = url("http://pavkin.ru")
    ),
    Developer(
      id = "jczuchnowski",
      name = "Jakub Czuchnowski",
      email = "jakub.czuchnowski@gmail.com",
      url = url("https://github.com/jczuchnowski")
    )
  ),
  releaseProcess := Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    runClean,
    runTest,
    setReleaseVersion,
    commitReleaseVersion,
    tagRelease,
    releaseStepCommandAndRemaining("+publishSigned"),
    releaseStepCommand("sonatypeBundleRelease"),
    setNextVersion,
    commitNextVersion,
    pushChanges
  )
)

val noPublishSettings = Seq(
  publish := {},
  publishLocal := {},
  publishArtifact := false
)

val core = (project in file("core"))
  .settings(buildSettings: _*)
  .settings(
    name := "akka-periscope-core",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor"   % akkaVersion,
      "org.scalatest"     %% "scalatest"    % scalaTestVersion % Test,
      "io.circe"          %% "circe-parser" % circeVersion % Test,
      "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test
    )
  )
  .settings(publishSettings: _*)

val akkaHttp = (project in file("akka-http"))
  .settings(buildSettings: _*)
  .settings(
    name := "akka-periscope-akka-http",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor"          % akkaVersion,
      "com.typesafe.akka" %% "akka-http"           % akkaHttpVersion,
      "org.scalatest"     %% "scalatest"           % scalaTestVersion % Test,
      "io.circe"          %% "circe-parser"        % circeVersion % Test,
      "com.typesafe.akka" %% "akka-testkit"        % akkaVersion % Test,
      "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test,
      "com.typesafe.akka" %% "akka-http-testkit"   % akkaHttpVersion % Test
    )
  )
  .settings(publishSettings: _*)
  .dependsOn(core % "compile->compile;test->test")

val root = (project in file("."))
  .settings(buildSettings: _*)
  .settings(publishSettings: _*)
  .settings(noPublishSettings: _*)
  .aggregate(core, akkaHttp)

def priorTo2_13(scalaVersion: String): Boolean =
  CrossVersion.partialVersion(scalaVersion) match {
    case Some((2, minor)) if minor < 13 => true
    case _                              => false
  }
