lazy val root = RootProject(file("../"))
// lazy val nameof = LocalProject("nameof")

// libs-scala
// lazy val `scala-utils` = LocalProject("scala-utils")
// lazy val `scalatest-utils` = LocalProject("scalatest-utils")
lazy val `logging-entries` = LocalProject("logging-entries")
lazy val `contextualized-logging` = LocalProject("contextualized-logging")
lazy val concurrent       = LocalProject("concurrent")
lazy val resources        = LocalProject("resources")
lazy val `resources-akka` = LocalProject("resources-akka")
lazy val `resources-grpc` = LocalProject("resources-grpc")

// lf
// lazy val interpreter = LocalProject("interpreter")
lazy val transaction = LocalProject("transaction")
// lazy val interface = LocalProject("interface")
lazy val `language` = LocalProject("language")
lazy val `kv-transaction-support` = LocalProject("kv-transaction-support")
// lazy val `data-scalacheck` = LocalProject("data-scalacheck")
// lazy val `transaction-test-lib` = LocalProject("transaction-test-lib")
// lazy val `scenario-interpreter` = LocalProject("scenario-interpreter")
lazy val data = LocalProject("data")
// lazy val parser = LocalProject("parser")
lazy val engine = LocalProject("engine")
lazy val validation = LocalProject("validation")
// lazy val repl = LocalProject("repl")
lazy val `archive-reader` = LocalProject("archive-reader")
// lazy val `archive-encoder` = LocalProject("archive-encoder")
// lazy val encoder = LocalProject("encoder")


// ledger-service
lazy val jwt = LocalProject("jwt")

// local projects:

lazy val `error` = project.settings(
    libraryDependencies ++= Seq(
        Deps.io_spray_spray_json,
        Deps.com_google_api_grpc_proto_google_common_protos,
        Deps.com_google_protobuf_protobuf_java,
        Deps.io_grpc_grpc_api,
        Deps.io_grpc_grpc_protobuf,
        Deps.org_slf4j_slf4j_api,
        )

// proto deps:
//"//ledger-api/grpc-definitions:ledger_api_proto_scala",
//"//ledger/participant-integration-api:participant-integration-api-proto_scala",

).dependsOn(
        // daml-lf
        `archive-reader`, data, engine, `language`, transaction, validation,
        //`participant-state`,
        `contextualized-logging`,`logging-entries`)

lazy val `ledger-api-common` = project

lazy val `ledger-api-auth` = project.settings(
    libraryDependencies ++= Seq(
        Deps.io_spray_spray_json,
        Deps.org_scala_lang_modules_scala_collection_compat,
        Deps.org_scala_lang_modules_scala_java8_compat,
        Deps.org_scalaz_scalaz_core,
        Deps.com_auth0_java_jwt,
        Deps.io_grpc_grpc_api,
        Deps.io_grpc_grpc_context,
        Deps.org_slf4j_slf4j_api)
).dependsOn(data, jwt, error, `ledger-api-common`, `contextualized-logging`)
        // ledger-api/grpc-definitions:ledger_api_proto_scala

//lazy val `caching` = project
//lazy val `cli-opts` = project
//lazy val `daml-on-sql` = project
//lazy val `indexer-benchmark` = project
//lazy val `ledger-api-akka` = project

//lazy val `ledger-api-auth-client` = project
//lazy val `ledger-api-bench-tool` = project
//lazy val `ledger-api-client` = project
//lazy val `ledger-api-domain` = project
//lazy val `ledger-api-health` = project
//lazy val `ledger-api-test-tool` = project
//lazy val `ledger-api-test-tool-on-canton` = project
//lazy val `ledger-configuration` = project
//lazy val `ledger-grpc` = project
//lazy val `ledger-offset` = project
//lazy val `ledger-on-memory` = project
//lazy val `ledger-on-sql` = project
//lazy val `ledger-resources` = project
//lazy val `metrics` = project
//lazy val `participant-integration-api` = project
//lazy val `participant-state-index` = project
//lazy val `participant-state-metrics` = project
//lazy val `recovering-indexer-integration-tests` = project
//lazy val `sandbox` = project
//lazy val `sandbox-classic` = project
//lazy val `sandbox-common` = project
//lazy val `sandbox-on-x` = project
//lazy val `sandbox-perf` = project
//lazy val `test-common` = project


// lazy val `participant-state` = project.settings(
//     libraryDependencies ++= Seq(
//         Deps.com_github_scopt_scopt,
//         Deps.com_typesafe_akka_akka_actor,
//         Deps.com_typesafe_akka_akka_stream,
//         Deps.org_scala_lang_modules_scala_collection_compat,
//         Deps.com_google_protobuf_protobuf_java,
//         Deps.io_dropwizard_metrics_metrics_core)

//         "//daml-lf/archive:daml_lf_1.dev_archive_proto_java",
//         "//ledger-api/grpc-definitions:ledger_api_proto_scala",

// ).dependsOn( 
//         `ledger-api-health`,
//         `ledger-configuration`,
//         `ledger-offset`,
//         `ledger-resources`,
//         `metrics`,
//         `participant-integration-api`,
//         `participant-state`,
//         `participant-state/kvutils`,
//         concurrent,
//         `contextualized-logging`,
//         resources,
//         `resources-akka`,
//         `resources-grpc`)