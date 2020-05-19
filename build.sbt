import ReleaseTransformations._
import ReleasePlugin.autoImport._

val akkaVersion      = "2.6.4"
val akkaHttpVersion  = "10.1.11"
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

lazy val publishSettings = Seq(
  releaseUseGlobalVersion := true,
  releaseVersionFile := file(".") / "version.sbt",
  releaseCommitMessage := s"Set version to ${version.value}",
  releaseIgnoreUntrackedFiles := true,
  releaseCrossBuild := true,
  homepage := Some(url("https://github.com/ScalaConsultants/akka-actor-tree")),
  licenses := Seq("MIT" -> url("https://opensource.org/licenses/MIT")),
  publishTo := sonatypePublishToBundle.value,
  publishMavenStyle := true,
  publishArtifact in Test := false,
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/ScalaConsultants/akka-actor-tree"),
      "scm:git:git@github.com:ScalaConsultants/akka-actor-tree.git"
    )
  ),
  developers := List(
    Developer(
      id = "vpavkin",
      name = "Vladimir Pavkin",
      email = "vpavkin@gmail.com",
      url = url("http://pavkin.ru")
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

val root = (project in file("."))
  .settings(
    organization := "io.scalac",
    name := "akka-actor-tree",
    scalaVersion := "2.13.2",
    crossScalaVersions := Seq("2.12.11", "2.13.2"),
    scalacOptions ++= {
      if (priorTo2_13(scalaVersion.value)) compilerOptions
      else
        compilerOptions.flatMap {
          case "-Ywarn-unused-import" => Seq("-Ywarn-unused:imports")
          case "-Xfuture"             => Nil
          case other                  => Seq(other)
        }
    },
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor"          % akkaVersion,
      "com.typesafe.akka" %% "akka-http"           % akkaHttpVersion,
      "org.scalatest"     %% "scalatest"           % scalaTestVersion % Test,
      "com.typesafe.akka" %% "akka-testkit"        % akkaVersion % Test,
      "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test,
      "com.typesafe.akka" %% "akka-http-testkit"   % akkaHttpVersion % Test
    )
  )
  .settings(publishSettings: _*)

def priorTo2_13(scalaVersion: String): Boolean =
  CrossVersion.partialVersion(scalaVersion) match {
    case Some((2, minor)) if minor < 13 => true
    case _                              => false
  }
