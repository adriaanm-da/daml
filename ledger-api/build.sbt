lazy val root = RootProject(file("../"))
// lazy val nameof = LocalProject("nameof")

// libs-scala
// lazy val `scala-utils` = LocalProject("scala-utils")
// lazy val `scalatest-utils` = LocalProject("scalatest-utils")
// lazy val `logging-entries` = LocalProject("logging-entries")
// lazy val `contextualized-logging` = LocalProject("contextualized-logging")
// lazy val concurrent       = LocalProject("concurrent")
// lazy val resources        = LocalProject("resources")
// lazy val `resources-akka` = LocalProject("resources-akka")
// lazy val `resources-grpc` = LocalProject("resources-grpc")



lazy val `vendored-grpc-protos` = (project in file("vendored-grpc-protos"))
  .settings(
// TODO: move vendoring of google protos into separate project, which also generates classfiles, 
// so we can drop com_google_api_grpc_proto_google_common_protos dependency
// Compile / PB.includePaths ++= Seq(resourceManaged.value / "googleapis-master"),
// Compile / PB.generate := (Compile / PB.generate).dependsOn(extractGoogleApiProtos).value,
// lazy val extractGoogleApiProtos = Def.task {
//     val dst = resourceManaged.value / "googleapis-master"
//     if (!dst.exists) {
//         val zipUrl = "https://github.com/googleapis/googleapis/archive/master.zip"
//         println(s"Unzipping $zipUrl to $dst.")
//         IO.unzipURL(
//             from=url(zipUrl),
//             filter=("googleapis-master/google/rpc/*.proto"),
//             toDirectory=resourceManaged.value)
//     }
// }

    libraryDependencies ++= Seq(
      Deps.com_google_api_grpc_proto_google_common_protos % "protobuf-src" intransitive(),
      "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf",
      "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion,
      Deps.io_grpc_grpc_api,
      Deps.io_grpc_grpc_core,
      Deps.io_grpc_grpc_stub,

    ),

    Compile / PB.targets := Seq(
       scalapb.gen() -> (Compile / sourceManaged).value,
       PB.gens.java  -> (Compile / sourceManaged).value),

  )

//         "//ledger-api/grpc-definitions:ledger_api_proto_scala",
lazy val `grpc-definitions` = project.settings(
    // tags = _maven_tags("com.daml", "ledger-api", "scalapb"),

    Compile / PB.targets := Seq(
        scalapb.gen() -> (Compile / sourceManaged).value,
        PB.gens.java  -> (Compile / sourceManaged).value),

).dependsOn(`vendored-grpc-protos`)