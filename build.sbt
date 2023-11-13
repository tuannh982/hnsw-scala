lazy val root = project
  .in(file("."))
  .aggregate(
    `core`,
    `bench`
  )
  .settings(
    name := "hnsw-scala-root",
    publish / skip := true
  )

lazy val `core`  = project
lazy val `bench` = project.dependsOn(`core`)
