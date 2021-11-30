lazy val `libs-scala` = project.
  in(file(".")).
  settings(publishArtifact := false).
  aggregate(`adjustable-clock`,
            `auth-utils`,
            `build-info`,
            `concurrent` ,
            `contextualized-logging`,
            `db-utils`,
            `doobie-slf4j`,
            `flyway-testing`,
            `fs-utils`,
            `gatling-utils`,
            `grpc-reverse-proxy`,
            `grpc-server-reflection-client`,
            `grpc-test-utils`,
            `grpc-utils`,
            `logging-entries`,
            `nameof` ,
            `oracle-testing`,
            `ports` ,
            `postgresql-testing`,
            `resources` ,
            `resources-akka`,
            `resources-grpc`,
            `scala-utils`,
            `scalatest-utils`,
            `target`,
            `timer-utils`)

lazy val `adjustable-clock` = project
lazy val `auth-utils` = project
lazy val `build-info` = project
lazy val `concurrent` = project
lazy val `contextualized-logging` = project
lazy val `db-utils` = project
lazy val `doobie-slf4j` = project
lazy val `flyway-testing` = project
lazy val `fs-utils` = project
lazy val `gatling-utils` = project
lazy val `grpc-reverse-proxy` = project
lazy val `grpc-server-reflection-client` = project
lazy val `grpc-test-utils` = project
lazy val `grpc-utils` = project
lazy val `logging-entries` = project.settings(libraryDependencies += Deps.io_spray_spray_json)
lazy val `nameof` = project.settings(libraryDependencies += "org.scala-lang" % "scala-compiler" % scalaVersion.value)
lazy val `oracle-testing` = project
lazy val `ports` = project
lazy val `postgresql-testing` = project
lazy val `resources` = project
lazy val `resources-akka` = project
lazy val `resources-grpc` = project
lazy val `scala-utils` = project.settings(
    libraryDependencies ++= Seq(Deps.org_scalaz_scalaz_core, Deps.org_scala_lang_modules_scala_collection_compat),
    Compile / unmanagedSourceDirectories += (baseDirectory( _ / "src/main/2.13" )).value,
    addCompilerPlugin(Deps.org_typelevel_kind_projector)
)

lazy val `scalatest-utils` = project.settings(
    libraryDependencies ++= Seq(
      Deps.org_scalacheck_scalacheck,
      Deps.org_scalactic_scalactic,
      Deps.org_scalatest_scalatest_core,
      Deps.org_scalatest_scalatest_flatspec,
      Deps.org_scalatest_scalatest_matchers_core,
      Deps.org_scalatest_scalatest_shouldmatchers,
      Deps.org_scalatest_scalatest_wordspec,
      Deps.org_scalatestplus_scalacheck_1_15,
      Deps.org_scalaz_scalaz_core,
      Deps.org_scalatest_scalatest_compatible),
    //     scalacopts = lf_scalacopts,

    addCompilerPlugin(Deps.org_typelevel_kind_projector)
)
// da_scala_test(
//     name = "test",
//     size = "small",
//     srcs = glob(["src/test/**/*.scala"]),
//     scala_deps = [
//         "@maven//:org_scalaz_scalaz_core",
//     ],
//     scalacopts = lf_scalacopts,
//     deps = [
//         ":scalatest-utils",
//     ],
// )

lazy val `target` = project
lazy val `timer-utils` = project