lazy val root = project.in(file("."))
  .settings(
    name := "hnsw-scala",
    organization := "io.github.tuannh982",
    versionScheme := Some("early-semver"),
    scalaVersion := "2.12.17",
    crossScalaVersions := Seq("2.12.17", "2.13.12", "3.3.1"),
    publish / skip := true
  )