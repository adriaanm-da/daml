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

//         "//ledger-api/grpc-definitions:ledger_api_proto_scala",
lazy val `grpc-definitions` = project.settings(
    // tags = _maven_tags("com.daml", "ledger-api", "scalapb"),
    
    // TODO
    libraryDependencies ++= Seq (
        Deps.com_google_protobuf_protobuf_java % "protobuf",
        /* brings in
  google/protobuf/any.proto
  google/protobuf/api.proto
  google/protobuf/compiler/plugin.proto
  google/protobuf/descriptor.proto
  google/protobuf/duration.proto
  google/protobuf/empty.proto
  google/protobuf/field_mask.proto
  google/protobuf/source_context.proto
  google/protobuf/struct.proto
  google/protobuf/timestamp.proto
  google/protobuf/type.proto
  google/protobuf/wrappers.proto
        */
        // TODO: replace this by our own class files? these dependencies seem pretty old?
        Deps.com_google_api_grpc_proto_google_common_protos % "protobuf", // for google/rpc/*.proto
        Deps.com_google_api_grpc_proto_google_common_protos, 
        Deps.io_grpc_grpc_api,
        Deps.io_grpc_grpc_core,
        Deps.io_grpc_grpc_stub,
        ),

    Compile / PB.targets := Seq(
        PB.gens.java  -> (Compile / sourceManaged).value),

)