name := "bench"

organization := ProjectInfo.organization
scalaVersion := ProjectInfo.scalaVersion
crossScalaVersions := ProjectInfo.crossScalaVersions
versionScheme := ProjectInfo.versionScheme

enablePlugins(JmhPlugin)