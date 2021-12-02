lazy val root = RootProject(file("../"))
lazy val nameof = LocalProject("nameof")
lazy val `scala-utils` = LocalProject("scala-utils")
lazy val `logging-entries` = LocalProject("logging-entries")
lazy val `scalatest-utils` = LocalProject("scalatest-utils")

lazy val `daml-lf` = project.aggregate(
  interpreter, transaction, interface, language, 
  `kv-transaction-support`, `data-scalacheck`, `transaction-test-lib`, 
  `scenario-interpreter`, data, parser, engine, validation, repl,
  `archive-reader`, `archive-encoder`, encoder)

lazy val data = project
  .settings(
    // other settings
    name := "daml-lf-data",
    libraryDependencies ++= List(
        Deps.org_scalaz_scalaz_core,
        Deps.com_google_guava_guava,
        Deps.com_google_protobuf_protobuf_java,
        Deps.org_slf4j_slf4j_api),

    libraryDependencies ++= List(
        Deps.org_scalacheck_scalacheck,
        Deps.org_scalatestplus_scalacheck_1_15,
        Deps.org_scalaz_scalaz_scalacheck_binding,
        Deps.org_scalatest_scalatest_freespec,
        Deps.org_scalatest_scalatest_propspec).map(_ % "test"),

// equivalent to `data-scalacheck` % "test" // https://github.com/sbt/sbt/issues/2698
    Test / unmanagedClasspath ++= (LocalProject("data-scalacheck") / Compile / fullClasspath).value,

    addCompilerPlugin(Deps.org_typelevel_kind_projector),

// for test scope:
//         silencer_plugin,
//     scalacopts = lf_scalacopts_stricter + [
//         "-P:silencer:lineContentFilters=import ImmArraySeq.Implicits._",
//     ],

  ).dependsOn(`logging-entries`,
              `scala-utils`,
              // `data-scalacheck` % "test", (see above)
              `scalatest-utils` % "test")


lazy val `data-scalacheck` = project
  .settings(
    name := "daml-lf-data-scalacheck",
//     scalacopts = lf_scalacopts_stricter,
    addCompilerPlugin(Deps.org_typelevel_kind_projector),
    libraryDependencies ++= Seq (
        Deps.org_scalacheck_scalacheck,
        Deps.org_scalaz_scalaz_core)
  ).dependsOn(LocalProject("data") % "compile")


lazy val language = project
  .settings(
    name := "daml-lf-language",
//     scalacopts = lf_scalacopts_stricter,
    libraryDependencies ++= Seq (
        Deps.org_scalaz_scalaz_core),

    libraryDependencies ++= List(
        Deps.org_scalatest_scalatest_shouldmatchers,
        Deps.org_scalatest_scalatest_wordspec).map(_ % "test"),

// for test scope:
//     plugins = [ silencer_plugin,     ],
//     scalacopts = lf_scalacopts + [
//         "-P:silencer:lineContentFilters=signum",
//     ],

  ).dependsOn(
        data,
        nameof)

lazy val parser = project
  .settings(
    // other settings
    name := "daml-lf-parser",
//         silencer_plugin,
//     scalacopts = lf_scalacopts_stricter + [
//         "-P:silencer:lineContentFilters=standardInterpolator",
    libraryDependencies ++= List(
        Deps.org_scala_lang_modules_scala_parser_combinators,
        Deps.org_scalaz_scalaz_core),

    libraryDependencies ++= List(
        Deps.org_scalatest_scalatest_shouldmatchers,
        Deps.org_scalatest_scalatest_wordspec,
        Deps.org_scalacheck_scalacheck,
        Deps.org_scalatestplus_scalacheck_1_15).map(_ % "test"),
  ).dependsOn(data,
              language)

//     visibility = [
//         "//daml-lf:__subpackages__",
//         "//ledger:__subpackages__",



lazy val interface = project
  .settings(
    name := "daml-lf-interface",
//         silencer_plugin,
//     scalacopts = lf_scalacopts_stricter,
    libraryDependencies ++= Seq(
        Deps.org_scala_lang_modules_scala_collection_compat,
        Deps.org_scalaz_scalaz_core,
        Deps.com_google_protobuf_protobuf_java),

    libraryDependencies ++= List(
        Deps.org_scalatest_scalatest_shouldmatchers,
        Deps.org_scalatest_scalatest_wordspec).map(_ % "test"),

  ).dependsOn(
           `archive-reader`,
//         "//daml-lf/archive:daml_lf_dev_archive_proto_java",
              data,
              language,
              parser % "test")

//     visibility = [
//         "//daml-assistant/daml-sdk:__subpackages__",
//         "//daml-lf:__subpackages__",
//         "//daml-script:__subpackages__",
//         "//extractor:__subpackages__",
//         "//language-support:__subpackages__",
//         "//ledger-service:__subpackages__",
//         "//navigator:__subpackages__",
//     ],


// Transaction library providing a high-level scala transaction
// data structure and associated utilities.
lazy val transaction = project
  .settings(
    // other settings
    name := "daml-lf-transaction",
    //         silencer_plugin,
//     scalacopts = lf_scalacopts_stricter,
    libraryDependencies ++= Seq(
        Deps.org_scala_lang_modules_scala_collection_compat,
        Deps.org_scalaz_scalaz_core,
        Deps.com_google_protobuf_protobuf_java,
        Deps.com_chuusai_shapeless                          % "test",
        Deps.org_scalacheck_scalacheck                      % "test",
        Deps.org_scalatestplus_scalacheck_1_15              % "test",
        Deps.org_scalaz_scalaz_core                         % "test",
        Deps.org_scalaz_scalaz_scalacheck_binding           % "test",
        Deps.org_scala_lang_modules_scala_collection_compat % "test",
        // TODO: the original protobuf dependencies were:
        // "@com_github_googleapis_googleapis//google/rpc:code_proto",
        // "@com_github_googleapis_googleapis//google/rpc:error_details_proto",
        // "@com_github_googleapis_googleapis//google/rpc:status_proto",
        // "@com_github_grpc_grpc//src/proto/grpc/health/v1:health_proto_descriptor",
        Deps.com_google_api_grpc_proto_google_common_protos
    ),

    // TODO: this compiles, but I'm not sure what the bazel plugin was actually doing...
    Compile / PB.targets := Seq(
        PB.gens.java  -> (Compile / sourceManaged).value),

    // test scope
    //     size = "medium",
    // TODO: ??? exclude "src/test/**/validation/*.scala" from main tests,
    // and bring it back as a separate test with restricted dependencies:
    //         "//daml-lf/data",
    //         "//daml-lf/language"
  
    libraryDependencies ++= List(
        Deps.org_scalatest_scalatest_shouldmatchers,
        Deps.org_scalatest_scalatest_freespec,
        Deps.org_scalatest_scalatest_wordspec).map(_ % "test"),

    // equivalent to `transaction-test-lib` % "test" // https://github.com/sbt/sbt/issues/2698
    Test / unmanagedClasspath ++= (LocalProject("transaction-test-lib") / Compile / fullClasspath).value,
  ).dependsOn(data,
              language,
              nameof,
              `scala-utils`,
//         ":transaction_proto_java",
//         ":value_proto_java",              
              interface % "test",
              // `transaction-test-lib` % "test" -- see above
              )

// TODO: publish protos
// proto_jars(
//     name = "value_proto",
//     srcs = ["src/main/protobuf/com/daml/lf/value.proto"],
//     maven_artifact_prefix = "daml-lf-value",
//     maven_group = "com.daml",
//     strip_import_prefix = "src/main/protobuf/",
//     visibility = ["//visibility:public"],
//     deps = [
//         "@com_google_protobuf//:empty_proto",
//     ],
// )
// proto_jars(
//     name = "transaction_proto",
//     srcs = ["src/main/protobuf/com/daml/lf/transaction.proto"],
//     maven_artifact_prefix = "daml-lf-transaction",
//     maven_group = "com.daml",
//     proto_deps = [
//         ":value_proto",
//     ],
//     strip_import_prefix = "src/main/protobuf/",
//     visibility = ["//visibility:public"],
// )


lazy val `kv-transaction-support` = project
  .settings(
    name := "daml-lf-kv-transaction-support",
//     scalacopts = lf_scalacopts_stricter,

    libraryDependencies ++= List(
      Deps.org_scalaz_scalaz_core,
      Deps.org_scalacheck_scalacheck,
      Deps.org_scalatest_scalatest_core,
      Deps.org_scalatest_scalatest_matchers_core,
      Deps.org_scalatest_scalatest_shouldmatchers,
      Deps.org_scalatest_scalatest_wordspec,
      Deps.org_scalatestplus_scalacheck_1_15,
      Deps.org_scalatest_scalatest_compatible).map(_ % "test"),

    Test / unmanagedClasspath ++= (LocalProject("transaction-test-lib") / Compile / fullClasspath).value,

  ).dependsOn(data,
              transaction)


lazy val `transaction-test-lib` = project
  .settings(
    name := "daml-lf-transaction-test-lib",
    addCompilerPlugin(Deps.org_typelevel_kind_projector),
//         silencer_plugin,
//     scalacopts = lf_scalacopts_stricter + [
//         "-P:silencer:lineContentFilters=import elt.injshrink",
//         # Forced upon us by Shrink
//         "-P:silencer:lineContentFilters=Stream.empty",
//     ],
    libraryDependencies ++= Seq (
        Deps.com_chuusai_shapeless,
        Deps.org_scalacheck_scalacheck,
        Deps.org_scalaz_scalaz_core,
        Deps.org_scalaz_scalaz_scalacheck_binding,
        Deps.org_scala_lang_modules_scala_collection_compat,
        Deps.com_google_protobuf_protobuf_java)
  ).dependsOn(data,
      `data-scalacheck`,
      interface,
      language,
      transaction)


lazy val validation = project
  .settings(
    // other settings
    name := "daml-lf-validation",
    //     scalacopts = lf_scalacopts_stricter,
    libraryDependencies ++= List(
        Deps.org_scala_lang_modules_scala_collection_compat,
        Deps.org_scalaz_scalaz_core),

    libraryDependencies ++= List(
        Deps.org_scalatest_scalatest_shouldmatchers,
        Deps.org_scalatest_scalatest_wordspec).map(_ % "test"),

    // test scope:
    //     plugins = [         silencer_plugin,
    //     scalacopts = lf_scalacopts + [
    //         "-P:silencer:lineContentFilters=standardInterpolator",
  ).dependsOn(data,
              language,
              `scala-utils`,
              parser % "test")

lazy val `typechecking-benchmark` = project.enablePlugins(JmhPlugin)
  .settings(
    Compile / unmanagedSourceDirectories := Seq((validation / baseDirectory).value / "src/bench"),

  ).dependsOn(
    validation,
    `archive-reader`,
  )

// da_scala_benchmark_jmh(
//     data = [
//         "//ledger/test-common:model-tests-1.14.dar",
//     ],
//     scala_deps = [
//         "Deps.org_scalaz_scalaz_core",
//     ],
//     deps = [
//         "//bazel_tools/runfiles:scala_runfiles",
//         "//daml-lf/archive:daml_lf_dev_archive_proto_java",
//         "//daml-lf/engine",
//         "//daml-lf/interpreter",
//         "//daml-lf/scenario-interpreter",
//         "//daml-lf/transaction",
//         "//ledger/test-common:dar-files-default-lib",
//         "Deps.com_google_protobuf_protobuf_java",
//     ],
// )


lazy val interpreter = project
  .settings(
    name := "daml-lf-interpreter",
    //     scalacopts = lf_scalacopts_stricter,
    libraryDependencies ++= Seq(
		    Deps.io_spray_spray_json,
        Deps.org_scala_lang_modules_scala_collection_compat,
        Deps.org_scalaz_scalaz_core,
        Deps.org_typelevel_paiges_core,
        Deps.com_google_protobuf_protobuf_java,
        Deps.org_slf4j_slf4j_api),

    libraryDependencies ++= Seq(
        Deps.org_scalacheck_scalacheck,
        Deps.org_scalatest_scalatest_core,
        Deps.org_scalatest_scalatest_matchers_core,
        Deps.org_scalatest_scalatest_shouldmatchers,
        Deps.org_scalatest_scalatest_wordspec,
        Deps.org_scalatest_scalatest_freespec,
        Deps.org_scalatestplus_scalacheck_1_15,
        Deps.org_scalaz_scalaz_scalacheck_binding,
        // TODO: ??? Deps.org_slf4j_slf4j_api -- Tests output "Failed to load class "org.slf4j.impl.StaticLoggerBinder"."
        ).map(_ % "test"),
        
    Test / unmanagedClasspath ++= (LocalProject("transaction-test-lib") / Compile / fullClasspath).value,

    // TODO: ??? split off this test
// da_scala_test(
//     name = "test_bignumeric",
//     srcs = ["src/test/scala/com/digitalasset/daml/lf/speedy/SBuiltinBigNumericTest.scala"],
//     scala_deps = [
//         "Deps.org_scalatest_scalatest_core",
//         "Deps.org_scalatest_scalatest_freespec",
//         "Deps.org_scalatest_scalatest_matchers_core",
//         "Deps.org_scalatest_scalatest_shouldmatchers",
//         "Deps.org_scalaz_scalaz_core",
//     ],
//     scalacopts = lf_scalacopts,
//     deps = [
//         ":interpreter",
//         "//daml-lf/data",
//         "//daml-lf/language",
//         "//daml-lf/parser",
//         "Deps.org_scalatest_scalatest_compatible",
//     ],
// )

  ).dependsOn(data,
              language,
              transaction,
              validation,
              nameof,
              `scala-utils`,
              interface % "test",
              parser % "test",
              `logging-entries` % "test")

//     visibility = [
//         "//compiler/repl-service:__subpackages__",
//         "//compiler/scenario-service:__subpackages__",
//         "//daml-lf:__subpackages__",
//         "//daml-script:__subpackages__",
//         "//extractor:__subpackages__",
//         "//ledger:__subpackages__",
//         "//triggers:__subpackages__",

// scala_repl(
//     name = "interpreter@repl",
//     deps = [
//         ":interpreter",
//     ],
// )




lazy val `scenario-interpreter` = project
  .settings(
    name := "daml-lf-scenario-interpreter",
//     main_class = "com.daml.lf.speedy.Main",
//     scalacopts = lf_scalacopts_stricter,
    libraryDependencies ++= Seq (
        Deps.org_typelevel_paiges_core),

    libraryDependencies ++= List(
        Deps.org_scalaz_scalaz_core,
        Deps.org_scalatest_scalatest_shouldmatchers,
        Deps.org_scalatest_scalatest_wordspec).map(_ % "test"),

  ).dependsOn(
        data,
        engine,
        interpreter,
        language,
        transaction,
        nameof,
        `scala-utils`)

//         "//daml-lf/engine",
//         "//daml-lf/interpreter",
//         "//daml-lf/language",
//         "//daml-lf/transaction",
//         "//libs-scala/scala-utils",

// daml_compile(
//     name = "CollectAuthority",
//     srcs = ["src/perf/resources/damls/CollectAuthority.daml"],
//     visibility = ["//visibility:public"],
// )

// # TODO (MK) Figure out what to do about the benchmark.

// da_scala_benchmark_jmh(
//     name = "scenario-perf",
//     srcs = glob(["src/perf/benches/**/*.scala"]),
//     data = [
//         ":CollectAuthority.dar",
//         ":CollectAuthority.dar.pp",
//     ],
//     scala_deps = ["Deps.org_scalaz_scalaz_core"],
//     visibility = ["//visibility:public"],
//     deps = [
//         "//bazel_tools/runfiles:scala_runfiles",
//         "//daml-lf/archive:daml_lf_archive_reader",
//         "//daml-lf/archive:daml_lf_dev_archive_proto_java",
//         "//daml-lf/data",
//         "//daml-lf/engine",
//         "//daml-lf/interpreter",
//         "//daml-lf/language",
//         "//daml-lf/scenario-interpreter",
//         "//daml-lf/transaction",
//         "Deps.com_google_protobuf_protobuf_java",
//     ],
// )

// da_scala_test(
//     name = "scenario-perf-test",
//     args = [
//         "-f",
//         "0",
//     ],
//     main_class = "org.openjdk.jmh.Main",
//     deps = [":scenario-perf"],
// )


lazy val engine = project
  .settings(
    // other settings
    name := "daml-lf-engine",
//     scalacopts = lf_scalacopts_stricter,
    libraryDependencies ++= List(
        Deps.org_typelevel_paiges_core,
        Deps.org_scalaz_scalaz_core,
        Deps.com_google_protobuf_protobuf_java)

  ).dependsOn(data,
              interpreter,
              language,
              transaction,
              validation,
              nameof,
              `scala-utils`)

// da_scala_test_suite(
//     name = "tests",
//     srcs = glob(
//         [
//             "src/test/**/*Spec.scala",
//             "src/test/**/*Test.scala",
//         ],
//         exclude = [
//             "src/test/**/LargeTransactionTest.scala",
//             "src/test/**/MinVersionTest.scala",
//         ],
//     ),
//     data = [
//         "//daml-lf/tests:AuthTests.dar",
//         "//daml-lf/tests:BasicTests.dar",
//         "//daml-lf/tests:Exceptions.dar",
//         "//daml-lf/tests:Interfaces.dar",
//         "//daml-lf/tests:MultiKeys.dar",
//         "//daml-lf/tests:Optional.dar",
//         "//daml-lf/tests:ReinterpretTests.dar",
//     ],
//     scala_deps = [
//         "Deps.com_storm_enroute_scalameter_core",
//         "Deps.org_scalatest_scalatest_core",
//         "Deps.org_scalatest_scalatest_matchers_core",
//         "Deps.org_scalatest_scalatest_shouldmatchers",
//         "Deps.org_scalatest_scalatest_wordspec",
//         "Deps.org_scalaz_scalaz_core",
//     ],
//     scalacopts = lf_scalacopts,
//     deps = [
//         ":engine",
//         "//bazel_tools/runfiles:scala_runfiles",
//         "//daml-lf/archive:daml_lf_archive_reader",
//         "//daml-lf/archive:daml_lf_dev_archive_proto_java",
//         "//daml-lf/data",
//         "//daml-lf/interpreter",
//         "//daml-lf/language",
//         "//daml-lf/parser",
//         "//daml-lf/transaction",
//         "//daml-lf/transaction-test-lib",
//         "//libs-scala/logging-entries",
//         "Deps.com_google_protobuf_protobuf_java",
//         "Deps.org_scalatest_scalatest_compatible",
//     ],
// )

// da_scala_test(
//     name = "test-large-transaction",
//     timeout = "moderate",
//     srcs = glob([
//         "src/test/**/LargeTransactionTest.scala",
//         "src/test/**/InMemoryPrivateLedgerData.scala",
//     ]),
//     data = [
//         "//daml-lf/tests:LargeTransaction.dar",
//     ],
//     # We setup a large heap size to reduce as much as possible GC overheads.
//     initial_heap_size = "2g",
//     max_heap_size = "2g",
//     scala_deps = [
//         "Deps.com_storm_enroute_scalameter_core",
//         "Deps.org_scalatest_scalatest_core",
//         "Deps.org_scalatest_scalatest_matchers_core",
//         "Deps.org_scalatest_scalatest_shouldmatchers",
//         "Deps.org_scalatest_scalatest_wordspec",
//         "Deps.org_scalaz_scalaz_core",
//     ],
//     scalacopts = lf_scalacopts,
//     deps = [
//         ":engine",
//         "//bazel_tools/runfiles:scala_runfiles",
//         "//daml-lf/archive:daml_lf_archive_reader",
//         "//daml-lf/data",
//         "//daml-lf/interpreter",
//         "//daml-lf/language",
//         "//daml-lf/transaction",
//         "Deps.org_scalatest_scalatest_compatible",
//     ],
// )

// da_scala_test(
//     name = "test-min-version",
//     srcs = glob([
//         "src/test/**/MinVersionTest.scala",
//     ]),
//     data = [
//         "//ledger/test-common:dar-files-1.14",
//     ],
//     scala_deps = [
//         "Deps.com_typesafe_akka_akka_actor",
//         "Deps.com_typesafe_akka_akka_stream",
//     ],
//     scalacopts = lf_scalacopts,
//     deps = [
//         "//bazel_tools/runfiles:scala_runfiles",
//         "//daml-lf/archive:daml_lf_archive_reader",
//         "//daml-lf/data",
//         "//daml-lf/language",
//         "//language-support/scala/bindings-akka",
//         "//ledger-api/rs-grpc-bridge",
//         "//ledger-api/testing-utils",
//         "//ledger/caching",
//         "//ledger/ledger-api-client",
//         "//ledger/ledger-api-common",
//         "//ledger/ledger-on-memory",
//         "//ledger/ledger-on-memory:ledger-on-memory-app",
//         "//ledger/ledger-resources",
//         "//ledger/participant-integration-api",
//         "//ledger/participant-state/kvutils/app",
//         "//ledger/test-common:dar-files-1.14-lib",
//         "//libs-scala/ports",
//         "//libs-scala/resources",
//         "Deps.com_google_protobuf_protobuf_java",
//         "Deps.io_netty_netty_handler",
//     ],
// )

lazy val repl = project
  .settings(
    name := "daml-lf-repl",
//     main_class = "com.daml.lf.speedy.testing.Main",
//     max_heap_size = "8g",
    libraryDependencies ++= Seq (
        Deps.org_scalaz_scalaz_core,
        Deps.org_typelevel_paiges_core,
        Deps.org_jline_jline)
  ).dependsOn(
        `archive-reader`,
        data,
        interpreter,
        language,
        parser,
        `scenario-interpreter`,
        transaction,
        validation)

lazy val `archive-reader` = (project in (file("archive")))
  .settings(
    name := "daml-lf-archive-reader",
//         silencer_plugin,
//     scalacopts = lf_scalacopts_stricter,
    libraryDependencies ++= Seq (
        Deps.org_scalaz_scalaz_core,
        Deps.org_scala_lang_modules_scala_collection_compat,
        Deps.com_google_protobuf_protobuf_java),
    Compile / PB.targets := Seq(
        PB.gens.java  -> (Compile / sourceManaged).value)
  ).dependsOn(
//         ":daml_lf_dev_archive_proto_java",
        data,
        language,
        nameof,
        `scala-utils`)



// da_scala_test_suite(
//     name = "daml_lf_archive_reader_tests",
//     size = "small",
//     srcs = glob(
//         ["src/test/scala/**/*.scala"],
//         exclude = ["src/test/scala/com/digitalasset/daml/lf/archive/ZipBombDetectionSpec.scala"],
//     ),
//     data = [
//         ":DarReaderTest.dalf",
//         ":DarReaderTest.dar",
//         ":daml_lf_1.11_archive_proto_srcs",
//         ":daml_lf_1.12_archive_proto_srcs",
//         ":daml_lf_1.13_archive_proto_srcs",
//         ":daml_lf_1.14_archive_proto_srcs",
//         ":daml_lf_1.6_archive_proto_srcs",
//         ":daml_lf_1.7_archive_proto_srcs",
//         ":daml_lf_1.8_archive_proto_srcs",
//     ],
//     scala_deps = [
//         "Deps.org_scalacheck_scalacheck",
//         "Deps.org_scalatest_scalatest_core",
//         "Deps.org_scalatest_scalatest_flatspec",
//         "Deps.org_scalatest_scalatest_matchers_core",
//         "Deps.org_scalatest_scalatest_shouldmatchers",
//         "Deps.org_scalatestplus_scalacheck_1_15",
//         "Deps.org_scalaz_scalaz_core",
//         "Deps.org_scalaz_scalaz_scalacheck_binding",
//     ],
//     scalacopts = lf_scalacopts,
//     deps = [
//         ":daml_lf_1.11_archive_proto_java",
//         ":daml_lf_1.12_archive_proto_java",
//         ":daml_lf_1.13_archive_proto_java",
//         ":daml_lf_1.14_archive_proto_java",
//         ":daml_lf_1.6_archive_proto_java",
//         ":daml_lf_1.7_archive_proto_java",
//         ":daml_lf_1.8_archive_proto_java",
//         ":daml_lf_archive_reader",
//         ":daml_lf_dev_archive_proto_java",
//         "//bazel_tools/runfiles:scala_runfiles",
//         "//daml-lf/data",
//         "//daml-lf/language",
//         "//daml-lf/transaction",
//         "//libs-scala/scalatest-utils",
//         "Deps.com_google_protobuf_protobuf_java",
//         "Deps.org_scalatest_scalatest_compatible",
//     ],
// )

// da_scala_test_suite(
//     name = "daml_lf_archive_reader_zipbomb_tests",
//     srcs = ["src/test/scala/com/digitalasset/daml/lf/archive/ZipBombDetectionSpec.scala"],
//     data = [
//         ":DarReaderTest.dar",
//     ],
//     scala_deps = [
//         "Deps.org_scalacheck_scalacheck",
//         "Deps.org_scalatestplus_scalacheck_1_15",
//     ],
//     scalacopts = lf_scalacopts,
//     deps = [
//         ":daml_lf_1.11_archive_proto_java",
//         ":daml_lf_1.6_archive_proto_java",
//         ":daml_lf_1.7_archive_proto_java",
//         ":daml_lf_1.8_archive_proto_java",
//         ":daml_lf_archive_reader",
//         ":daml_lf_dev_archive_proto_java",
//         "//bazel_tools/runfiles:scala_runfiles",
//         "//daml-lf/data",
//         "Deps.com_google_protobuf_protobuf_java",
//     ],
// )

// daml_compile_with_dalf(
//     name = "DarReaderTest",
//     srcs = ["src/test/daml/DarReaderTest.daml"],
// )

// # An ad-hoc tool for testing, benchmarking and profiling package decoding performance in isolation.
// da_scala_binary(
//     name = "decode-tester",
//     srcs = ["src/test/scala/com/digitalasset/daml/lf/archive/DecodeMain.scala"],
//     main_class = "com.daml.lf.archive.DecodeMain",
//     deps = [
//         ":daml_lf_archive_reader",
//         "//daml-lf/data",
//         "//daml-lf/language",
//     ],
// )


// ## BUILD AT ./archive/encoder/BUILD.bazel
lazy val `archive-encoder` = (project in (file("archive/encoder")))
  .settings(
    // publish := false
    libraryDependencies ++= List(
        Deps.org_scalaz_scalaz_core)
  ).dependsOn(`archive-reader`)


// ## BUILD AT ./encoder/BUILD.bazel

lazy val encoder = project
  .settings(
    // other settings
    // publish := false
    //         exclude = [],
//     scalacopts = lf_scalacopts,
    unmanagedSources / excludeFilter := new sbt.io.ExactFileFilter(
      baseDirectory.value / "src/main/scala/com/digitalasset/daml/lf/archive/testing/DamlLfEncoder.scala"),
    
    libraryDependencies ++= List(
        Deps.org_scalaz_scalaz_core)
        //         "Deps.com_google_protobuf_protobuf_java",

  ).dependsOn(`archive-reader`, data, language)

// da_scala_test_suite(
//     name = "tests",
//     size = "small",
//     srcs = glob(["src/test/scala/**/*.scala"]),
//     data = [":testing-dar-%s" % target for target in ENCODER_LF_VERSIONS],
//     scala_deps = [
//         "Deps.org_scalatest_scalatest_core",
//         "Deps.org_scalatest_scalatest_matchers_core",
//         "Deps.org_scalatest_scalatest_shouldmatchers",
//         "Deps.org_scalatest_scalatest_wordspec",
//         "Deps.org_scalaz_scalaz_core",
//     ],
//     scalacopts = lf_scalacopts,
//     deps = [
//         ":encoder",
//         "//bazel_tools/runfiles:scala_runfiles",
//         "//daml-lf/archive:daml_lf_archive_reader",
//         "//daml-lf/archive:daml_lf_dev_archive_proto_java",
//         "//daml-lf/data",
//         "//daml-lf/language",
//         "//daml-lf/parser",
//         "//daml-lf/validation",
//         "//libs-scala/logging-entries",
//         "Deps.com_google_protobuf_protobuf_java",
//         "Deps.org_scalatest_scalatest_compatible",
//     ],
// )

// da_scala_binary(
//     name = "encoder_binary",
//     srcs = glob(["src/main/scala/com/digitalasset/daml/lf/archive/testing/DamlLfEncoder.scala"]),
//     main_class = "com.daml.lf.archive.testing.DamlLfEncoder",
//     scalacopts = lf_scalacopts,
//     visibility = ["//visibility:public"],
//     deps = [
//         ":encoder",
//         "//:sdk-version-scala-lib",
//         "//daml-lf/archive:daml_lf_archive_reader",
//         "//daml-lf/archive:daml_lf_dev_archive_proto_java",
//         "//daml-lf/archive/encoder",
//         "//daml-lf/data",
//         "//daml-lf/language",
//         "//daml-lf/parser",
//         "//daml-lf/validation",
//         "Deps.com_google_protobuf_protobuf_java",
//     ],
// )

// [
//     filegroup(
//         name = "lf_%s" % target,
//         srcs = glob([
//             "src/test/lf/*_all_*.lf",
//             "src/test/lf/*_%s_*.lf" % target,
//         ]),
//     )
//     for target in ENCODER_LF_VERSIONS
// ]

// [
//     [
//         genrule(
//             name = "testing-dar-%s" % target,
//             srcs = [":lf_%s" % target],
//             outs = ["test-%s.dar" % target],
//             cmd = "$(location :encoder_binary) $(SRCS) --output $@ --target %s" % target,
//             tools = [":encoder_binary"],
//             visibility = ["//visibility:public"],
//         ),
//         sh_test(
//             name = "validate-dar-%s" % target,
//             srcs = ["src/validate.sh"],
//             args = [
//                 "$(location //daml-lf/repl:repl)",
//                 "$(location :testing-dar-%s)" % target,
//                 "--dev" if (target == lf_version_configuration.get("dev") or target == lf_version_configuration.get("preview")) else "",
//             ],
//             data = [
//                 "//daml-lf/repl",
//                 "testing-dar-%s" % target,
//             ],
//             deps = [
//                 "@bazel_tools//tools/bash/runfiles",
//             ],
//         ),
//     ]
//     for target in ENCODER_LF_VERSIONS
// ]

// [
//     [
//         genrule(
//             name = "testing-dar-lookup-scala-%s" % keyword,
//             outs = ["TestDars-%s.scala" % mangle_for_java(keyword)],
//             cmd = """
// cat > $@ <<EOF
// package com.daml.lf.archive.testing
// object TestDar {
//     val fileName = \"daml-lf/encoder/test-%s.dar\"
// }
// EOF
// """ % version,
//         ),
//         da_scala_library(
//             name = "testing-dar-lookup-lib-%s" % keyword,
//             srcs = ["testing-dar-lookup-scala-%s" % keyword],
//             # generated_srcs is required for scaladocF
//             generated_srcs = ["testing-dar-lookup-scala-%s" % keyword],
//             visibility = ["//visibility:public"],
//         ),
//     ]
//     for (keyword, version) in lf_version_configuration.items()
//     if keyword in ["latest"]
// ]

// [
//     alias(
//         name = "testing-dar-%s" % keyword,
//         actual = ":testing-dar-%s" % version,
//         visibility = ["//visibility:public"],
//     )
//     for (keyword, version) in lf_version_configuration.items()
// ]

// filegroup(
//     name = "testing-dars",
//     srcs = ["testing-dar-%s" % version for version in ENCODER_LF_VERSIONS],
//     visibility = ["//visibility:public"],
// )




// ## BUILD AT ./interpreter/perf/BUILD.bazel

// load(
//     "//bazel_tools:scala.bzl",
//     "da_scala_binary",
// )
// load("//rules_daml:daml.bzl", "daml_compile")

// daml_compile(
//     name = "Examples",
//     srcs = glob(["daml/Examples.daml"]),
// )

// da_scala_binary(
//     name = "explore",
//     srcs = glob(["src/main/**/Explore.scala"]),
//     main_class = "com.daml.lf.speedy.explore.Explore",
//     runtime_deps = [
//         "Deps.ch_qos_logback_logback_classic",
//     ],
//     deps = [
//         "//daml-lf/data",
//         "//daml-lf/interpreter",
//         "//daml-lf/language",
//     ],
// )

// da_scala_binary(
//     name = "explore-dar",
//     srcs = glob(["src/main/**/ExploreDar.scala"]),
//     data = [
//         ":Examples.dar",
//         ":Examples.dar.pp",
//         ":JsonParser.dar",
//         ":JsonParser.dar.pp",
//     ],
//     main_class = "com.daml.lf.speedy.explore.ExploreDar",
//     scala_deps = [
//         "Deps.org_scalaz_scalaz_core",
//     ],
//     runtime_deps = [
//         "Deps.ch_qos_logback_logback_classic",
//     ],
//     deps = [
//         "//bazel_tools/runfiles:scala_runfiles",
//         "//daml-lf/archive:daml_lf_archive_reader",
//         "//daml-lf/data",
//         "//daml-lf/interpreter",
//         "//daml-lf/language",
//     ],
// )

// da_scala_binary(
//     name = "speed-nfib",
//     srcs = glob([
//         "src/main/**/LoadDarFunction.scala",
//         "src/main/**/SpeedTestNfib.scala",
//     ]),
//     data = [
//         ":Examples.dar",
//         ":Examples.dar.pp",
//     ],
//     main_class = "com.daml.lf.speedy.explore.SpeedTestNfib",
//     scala_deps = [
//         "Deps.org_scalaz_scalaz_core",
//     ],
//     runtime_deps = [
//         "Deps.ch_qos_logback_logback_classic",
//     ],
//     deps = [
//         "//bazel_tools/runfiles:scala_runfiles",
//         "//daml-lf/archive:daml_lf_archive_reader",
//         "//daml-lf/data",
//         "//daml-lf/interpreter",
//         "//daml-lf/language",
//     ],
// )

// daml_compile(
//     name = "JsonParser",
//     srcs = glob(["daml/JsonParser.daml"]),
// )

// da_scala_binary(
//     name = "speed-json-parser",
//     srcs = glob([
//         "src/main/**/LoadDarFunction.scala",
//         "src/main/**/SpeedTestJsonParser.scala",
//     ]),
//     data = [
//         ":JsonParser.dar",
//         ":JsonParser.dar.pp",
//     ],
//     main_class = "com.daml.lf.speedy.explore.SpeedTestJsonParser",
//     scala_deps = [
//         "Deps.org_scalaz_scalaz_core",
//     ],
//     runtime_deps = [
//         "Deps.ch_qos_logback_logback_classic",
//     ],
//     deps = [
//         "//bazel_tools/runfiles:scala_runfiles",
//         "//daml-lf/archive:daml_lf_archive_reader",
//         "//daml-lf/data",
//         "//daml-lf/interpreter",
//         "//daml-lf/language",
//     ],
// )


// ## BUILD AT ./tests/BUILD.bazel

// load(
//     "//rules_daml:daml.bzl",
//     "daml_build_test",
//     "daml_compile",
// )
// load(
//     "//daml-lf/language:daml-lf.bzl",
//     "lf_version_configuration",
// )

// TEST_FILES = \
//     [
//         "BasicTests",
//         "AuthorizedDivulgence",
//         "DontDiscloseNonConsumingExercisesToObservers",
//         "LargeTransaction",
//         "ConjunctionChoices",
//         "ContractKeys",
//         "AuthTests",
//     ]

// [
//     daml_compile(
//         name = name,
//         srcs = ["%s.daml" % name],
//         visibility = ["//daml-lf:__subpackages__"],
//     )
//     for name in TEST_FILES
// ]

// daml_compile(
//     name = "Optional",
//     srcs = ["Optional.daml"],
//     visibility = ["//daml-lf:__subpackages__"],
// )

// daml_compile(
//     name = "Exceptions",
//     srcs = ["Exceptions.daml"],
//     visibility = ["//daml-lf:__subpackages__"],
// )

// daml_compile(
//     name = "Interfaces",
//     srcs = ["Interfaces.daml"],
//     target = "1.dev",
//     visibility = ["//daml-lf:__subpackages__"],
// )

// daml_build_test(
//     name = "ReinterpretTests",
//     dar_dict = {
//         "//daml-lf/tests:AtVersion13.dar": "at-version-13.dar",
//         "//daml-lf/tests:AtVersion14.dar": "at-version-14.dar",
//     },
//     project_dir = "reinterpret",
//     visibility = ["//daml-lf:__subpackages__"],
// )

// daml_compile(
//     name = "AtVersion13",
//     srcs = ["reinterpret/AtVersion13.daml"],
//     target = "1.13",
//     visibility = ["//daml-lf:__subpackages__"],
// )

// daml_compile(
//     name = "AtVersion14",
//     srcs = ["reinterpret/AtVersion14.daml"],
//     target = "1.14",
//     visibility = ["//daml-lf:__subpackages__"],
// )

// daml_compile(
//     name = "MultiKeys",
//     srcs = ["MultiKeys.daml"],
//     # TODO https://github.com/digital-asset/daml/issues/9914
//     # Drop line once LF 1.14 is the default compiler output.
//     target = lf_version_configuration.get("latest"),
//     visibility = ["//daml-lf:__subpackages__"],
// )

// [
//     sh_test(
//         name = name + "-test",
//         size = "small",
//         srcs = ["daml-lf-test.sh"],
//         args = [
//             "$(location //daml-lf/repl:repl)",
//             "$(location //compiler/damlc:damlc-compile-only)",
//             "$(location :%s.dar)" % name,
//         ],
//         data = [
//             "//compiler/damlc:damlc-compile-only",
//             "//daml-lf/repl",
//             ":%s.dar" % name,
//         ],
//         deps = [
//             "@bazel_tools//tools/bash/runfiles",
//         ],
//     )
//     for name in TEST_FILES
// ]

// [
//     sh_test(
//         name = "test-scenario-stable-" + file.split("/")[2],
//         size = "medium",
//         srcs = ["scenario/test.sh"],
//         args = [
//             "$(location //daml-lf/repl:repl)",
//             "$(location //compiler/damlc:damlc-compile-only)",
//             "$(location :%s)" % file,
//             "$(POSIX_DIFF)",
//             "false",
//         ],
//         data = [
//             "//compiler/damlc:damlc-compile-only",
//             "//daml-lf/repl",
//             file,
//             "%s/EXPECTED.ledger" % "/".join(file.split("/")[0:3]),
//         ],
//         toolchains = [
//             "@rules_sh//sh/posix:make_variables",
//         ],
//         deps = [
//             "@bazel_tools//tools/bash/runfiles",
//         ],
//     )
//     for file in glob(["scenario/stable/*/Test.daml"])
// ]

// [
//     sh_test(
//         name = "test-scenario-dev-" + file.split("/")[2],
//         size = "medium",
//         srcs = ["scenario/test.sh"],
//         args = [
//             "$(location //daml-lf/repl:repl)",
//             "$(location //compiler/damlc:damlc-compile-only)",
//             "$(location :%s)" % file,
//             "$(POSIX_DIFF)",
//             "true",
//         ],
//         data = [
//             "//compiler/damlc:damlc-compile-only",
//             "//daml-lf/repl",
//             file,
//             "%s/EXPECTED.ledger" % "/".join(file.split("/")[0:3]),
//         ],
//         toolchains = [
//             "@rules_sh//sh/posix:make_variables",
//         ],
//         deps = [
//             "@bazel_tools//tools/bash/runfiles",
//         ],
//     )
//     for file in glob(["scenario/dev/*/Test.daml"])
// ]