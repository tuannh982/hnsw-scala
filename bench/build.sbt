name := "bench"

organization := ProjectInfo.organization
scalaVersion := ProjectInfo.scalaVersion
versionScheme := ProjectInfo.versionScheme

enablePlugins(JmhPlugin)

libraryDependencies ++= Dependencies.testDependencies
