import sbt._

object ProjectInfo {
  val organization                    = "io.github.tuannh982"
  val buildVersion                    = "0.0.1-SNAPSHOT"
  val scalaVersion                    = "2.12.17"
  val crossScalaVersions: Seq[String] = Seq("2.12.17", "2.13.12", "3.3.1")
  val versionScheme: Option[String]   = Some("early-semver")
}

object Dependencies {

  val testDependencies: Seq[ModuleID] = Seq(
    "org.scalatest" %% "scalatest" % "3.2.15" % "test",
    "org.scalamock" %% "scalamock" % "5.2.0" % "test"
  )
}
