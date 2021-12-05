import sbt._

object Deps {
    val akka_version = "2.6.13"
    val netty_version = "4.1.67.Final"
	val grpc_version = "1.41.0"

	lazy val ch_qos_logback_logback_classic                  = "ch.qos.logback" % "logback-classic" % "1.2.3"
	lazy val com_chuusai_shapeless                           = "com.chuusai" %% "shapeless" % "2.3.3"
	lazy val com_google_guava_guava                          = "com.google.guava" % "guava" % "29.0-jre"
	lazy val com_google_protobuf_protobuf_java               = "com.google.protobuf" % "protobuf-java" % "3.17.3"
	lazy val com_storm_enroute_scalameter_core               = "com.storm-enroute" %% "scalameter-core" % "0.19"
	lazy val com_typesafe_akka_akka_actor                    = "com.typesafe.akka" %% "akka-actor" % akka_version
	lazy val com_typesafe_akka_akka_stream                   = "com.typesafe.akka" %% "akka-stream" % akka_version
	lazy val io_netty_netty_handler                          = "io.netty" % "netty-handler" % netty_version
	lazy val io_spray_spray_json                             = "io.spray" %% "spray-json" % "1.3.5"
	lazy val org_jline_jline                                 = "org.jline" % "jline" % "3.7.1"
	lazy val org_scala_lang_modules_scala_collection_compat  = "org.scala-lang.modules" %% "scala-collection-compat" % "2.3.2"
	lazy val org_scala_lang_modules_scala_parser_combinators = "org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.2"
	lazy val org_scalacheck_scalacheck                       = "org.scalacheck" %% "scalacheck" % "1.15.4"

	lazy val org_scalatest_scalatest                         = "org.scalatest" %% "scalatest-core" % "3.2.9"

 	lazy val org_scalatest_scalatest_core                    = "org.scalatest" %% "scalatest-core" % "3.2.9"
 	lazy val org_scalatest_scalatest_compatible              = "org.scalatest" %  "scalatest-compatible" % "3.2.9"
 	lazy val org_scalatest_scalatest_flatspec                = "org.scalatest" %% "scalatest-flatspec" % "3.2.9"
 	lazy val org_scalatest_scalatest_freespec                = "org.scalatest" %% "scalatest-freespec" % "3.2.9"
	lazy val org_scalatest_scalatest_propspec                = "org.scalatest" %% "scalatest-propspec" % "3.2.9"
 	lazy val org_scalatest_scalatest_matchers_core           = "org.scalatest" %% "scalatest-matchers-core" % "3.2.9"
 	lazy val org_scalatest_scalatest_shouldmatchers          = "org.scalatest" %% "scalatest-shouldmatchers" % "3.2.9"
 	lazy val org_scalatest_scalatest_wordspec                = "org.scalatest" %% "scalatest-wordspec" % "3.2.9"
	lazy val org_scalatestplus_scalacheck_1_15               = "org.scalatestplus" %% "scalacheck-1-15" % "3.2.9.0"
	lazy val org_scalactic_scalactic                         = "org.scalactic" %% "scalactic" % "3.2.9"


	lazy val org_scalaz_scalaz_core                          = "org.scalaz" %% "scalaz-core" % "7.2.33"
	lazy val org_scalaz_scalaz_scalacheck_binding            = "org.scalaz" %% "scalaz-scalacheck-binding" % "7.2.33-scalacheck-1.15"

	lazy val org_slf4j_slf4j_api                             = "org.slf4j" % "slf4j-api" % "1.7.26"
	lazy val org_typelevel_paiges_core                       = "org.typelevel" %% "paiges-core" % "0.3.2"

	lazy val org_typelevel_kind_projector                    = "org.typelevel" % "kind-projector" % "0.13.2" cross CrossVersion.full

	// ledger
	lazy val org_scala_lang_modules_scala_java8_compat       = "org.scala-lang.modules" %% "scala-java8-compat" % "0.9.0"
	lazy val com_auth0_java_jwt = "com.auth0" % "java-jwt" % "3.10.3"
	lazy val com_auth0_jwks_rsa = "com.auth0" % "jwks-rsa" % "0.11.0"


	// Bumping versions of io.grpc" % "* has a few implications" % "
	// 1. io.grpc" % "grpc-protobuf has a dependency on com.google.protobuf" % "protobuf-java, which in
	//    turn needs to be aligned with the version of protoc we are using (as declared in deps.bzl).
	//    ScalaPB also depends on a specific version of protobuf-java, but it's not strict" % "
	//    as long as the version we use is greater than or equal to the version required by ScalaPB,
	//    everything should work.
	//
	// 2. To keep TLS for the Ledger API Server working, the following three artifacts need be updated
	// in sync according to https" % "//github.com/grpc/grpc-java/blob/master/SECURITY.md#netty
	//
	// * io.grpc" % "grpc-netty
	// * io.netty" % "netty-handler
	// * io.netty" % "netty-tcnative-boringssl-static
	//
	// This effectively means all io.grpc" % "*, io.netty" % "*, and `com.google.protobuf" % "protobuf-java
	// need to be updated with careful consideration.
	// grpc
	lazy val io_grpc_grpc_api = "io.grpc" % "grpc-api" % grpc_version
	lazy val io_grpc_grpc_core = "io.grpc" % "grpc-core" % grpc_version
	lazy val io_grpc_grpc_netty = "io.grpc" % "grpc-netty" % grpc_version
	lazy val io_grpc_grpc_protobuf = "io.grpc" % "grpc-protobuf" % grpc_version
	lazy val io_grpc_grpc_services = "io.grpc" % "grpc-services" % grpc_version
	lazy val io_grpc_grpc_stub = "io.grpc" % "grpc-stub" % grpc_version
	lazy val io_grpc_grpc_context =	"io.grpc" % "grpc-context" % grpc_version
	
	lazy val com_google_api_grpc_proto_google_common_protos = "com.google.api.grpc"    % "googleapis-common-protos" % "0.0.3"

	lazy val com_typesafe_scala_logging_scala_logging = "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"
	
	lazy val com_github_scopt_scopt = "com.github.scopt" %% "scopt" % "4.0.0"

	lazy val com_fasterxml_jackson_core_jackson_core = "com.fasterxml.jackson.core" % "jackson-core" % "2.12.0"
// lazy val com_fasterxml_jackson_core_jackson_databind = "com.fasterxml.jackson.core" % "jackson-databind" % "2.12.0"

	lazy val net_logstash_logback_logstash_logback_encoder = "net.logstash.logback" % "logstash-logback-encoder" % "6.6"

/*
lazy val ch_qos_logback_logback_classic =
lazy val ch_qos_logback_logback_core =
lazy val com_chuusai_shapeless =
lazy val com_github_ben_manes_caffeine_caffeine =

lazy val com_google_guava_guava =
lazy val com_google_protobuf_protobuf_java =
lazy val com_h2database_h2 =
lazy val com_lihaoyi_fansi =
lazy val com_lihaoyi_pprint =
lazy val com_oracle_database_jdbc_ojdbc8 =
lazy val com_typesafe_akka_akka_actor =
lazy val com_typesafe_akka_akka_actor_testkit_typed =
lazy val com_typesafe_akka_akka_actor_typed =
lazy val com_typesafe_akka_akka_slf4j =
lazy val com_typesafe_akka_akka_stream =
lazy val com_typesafe_akka_akka_stream_testkit =
lazy val com_typesafe_akka_akka_testkit =
lazy val com_typesafe_config =
lazy val com_zaxxer_HikariCP =
lazy val commons_codec_commons_codec =
lazy val commons_io_commons_io =
lazy val io_circe_circe_core =
lazy val io_circe_circe_yaml =
lazy val io_dropwizard_metrics_metrics_core =
lazy val io_dropwizard_metrics_metrics_graphite =
lazy val io_dropwizard_metrics_metrics_jmx =
lazy val io_dropwizard_metrics_metrics_jvm =
lazy val io_netty_netty_buffer =
lazy val io_netty_netty_common =
lazy val io_netty_netty_handler =
lazy val io_netty_netty_tcnative_boringssl_static =
lazy val io_netty_netty_transport =
lazy val io_opentelemetry_opentelemetry_api =
lazy val io_opentelemetry_opentelemetry_context =
lazy val io_opentelemetry_opentelemetry_sdk_testing =
lazy val io_opentelemetry_opentelemetry_sdk_trace =
lazy val io_opentelemetry_opentelemetry_semconv =
lazy val io_prometheus_simpleclient =
lazy val io_prometheus_simpleclient_dropwizard =
lazy val io_prometheus_simpleclient_httpserver =
lazy val io_spray_spray_json =
lazy val junit_junit =
lazy val org_awaitility_awaitility =
lazy val org_flywaydb_flyway_core =
lazy val org_mockito_mockito_core =
lazy val org_mockito_mockito_scala =
lazy val org_playframework_anorm_anorm =
lazy val org_playframework_anorm_anorm_tokenizer =
lazy val org_postgresql_postgresql =
lazy val org_reactivestreams_reactive_streams =
lazy val org_reflections_reflections =
lazy val org_scala_lang_modules_scala_collection_compat =
lazy val org_scala_lang_modules_scala_java8_compat =
lazy val org_scala_lang_modules_scala_parallel_collections =
lazy val org_scalacheck_scalacheck =
lazy val org_scalactic_scalactic =
lazy val org_scalameta_munit =
lazy val org_scalatest_scalatest =
lazy val org_scalatest_scalatest_compatible =
lazy val org_scalatest_scalatest_core =
lazy val org_scalatest_scalatest_flatspec =
lazy val org_scalatest_scalatest_matchers_core =
lazy val org_scalatest_scalatest_shouldmatchers =
lazy val org_scalatest_scalatest_wordspec =
lazy val org_scalatestplus_scalacheck_1_15 =
lazy val org_scalaz_scalaz_core =
lazy val org_slf4j_slf4j_api =
lazy val org_typelevel_cats_core =
lazy val org_xerial_sqlite_jdbc =

*/
}

/*
"ch.qos.logback" % "logback-core" % "1.2.3",
"com.github.ben-manes.caffeine" % "caffeine" % "2.8.0",
"com.github.ghik" % "silencer-plugin" %%% "1.7.5"
"com.github.pureconfig" % "pureconfig" %% "0.14.0"
"com.github.pureconfig" % "pureconfig-core" %% "0.14.0"
"com.github.pureconfig" % "pureconfig-generic" %% "0.14.0"
"com.github.pureconfig" % "pureconfig-macros" %% "0.14.0"
"com.google.code.findbugs" % "jsr305" % "3.0.2",
"com.google.code.gson" % "gson" % "2.8.2",
"com.h2database" % "h2" % "1.4.200",
"com.lihaoyi" % "pprint" %% "0.6.0"
"com.lihaoyi" % "sjsonnet" %% "0.3.0"
"commons-io" % "commons-io" % "2.5",
"com.oracle.database.jdbc" % "ojdbc8" % "19.8.0.0",
"com.sparkjava" % "spark-core" % "2.9.1",
"com.oracle.database.jdbc.debug" % "ojdbc8_g" % "19.8.0.0",
"com.squareup" % "javapoet" % "1.11.1",
"com.storm-enroute" % "scalameter" %% "0.19"


"com.typesafe.akka" % "akka-actor-testkit-typed" %% akka_version
"com.typesafe.akka" % "akka-actor-typed" %% akka_version
"com.typesafe.akka" % "akka-http" %% "10.2.1"
"com.typesafe.akka" % "akka-http-spray-json" %% "10.2.1"
"com.typesafe.akka" % "akka-http-testkit" %% "10.2.1"
"com.typesafe.akka" % "akka-slf4j" %% akka_version

"com.typesafe.akka" % "akka-stream-testkit" %% akka_version
"com.typesafe.akka" % "akka-testkit" %% akka_version
"org.playframework.anorm" % "anorm" %% "2.6.8"
"org.playframework.anorm" % "anorm-akka" %% "2.6.8"
"com.zaxxer" % "HikariCP" % "3.2.0",
"eu.rekawek.toxiproxy" % "toxiproxy-java" % "2.1.3",
"io.circe" % "circe-core" %% "0.13.0"
"io.circe" % "circe-generic" %% "0.13.0"
"io.circe" % "circe-parser" %% "0.13.0"
"io.circe" % "circe-yaml" %% "0.13.0"
"io.dropwizard.metrics" % "metrics-core" % "4.1.2",
"io.dropwizard.metrics" % "metrics-graphite" % "4.1.2",
"io.dropwizard.metrics" % "metrics-jmx" % "4.1.2",
"io.dropwizard.metrics" % "metrics-jvm" % "4.1.2",
"io.opentelemetry" % "opentelemetry-api" % "0.16.0",
"io.opentelemetry" % "opentelemetry-context" % "0.16.0",
"io.opentelemetry" % "opentelemetry-sdk-testing" % "0.16.0",
"io.opentelemetry" % "opentelemetry-sdk-trace" % "0.16.0",
"io.opentelemetry" % "opentelemetry-semconv" % "0.16.0-alpha",
"io.prometheus" % "simpleclient" % "0.8.1",
"io.prometheus" % "simpleclient_dropwizard" % "0.8.1",
"io.prometheus" % "simpleclient_httpserver" % "0.8.1",
"io.prometheus" % "simpleclient_servlet" % "0.8.1",



"io.netty" % "netty-buffer" % netty_version
"io.netty" % "netty-codec-http2" % netty_version

"io.netty" % "netty-handler-proxy" % netty_version
"io.netty" % "netty-resolver" % netty_version
"io.netty" % "netty-tcnative-boringssl-static" % netty_tcnative_version


"com.thesamet.scalapb" % "compilerplugin" %% scalapb_version
"com.thesamet.scalapb" % "lenses" %% scalapb_version
"com.thesamet.scalapb" % "protoc-bridge" %% scalapb_protoc_version
"com.thesamet.scalapb" % "protoc-gen" %% scalapb_protoc_version
"com.thesamet.scalapb" % "scalapb-runtime" %% scalapb_version
"com.thesamet.scalapb" % "scalapb-runtime-grpc" %% scalapb_version

"io.gatling" % "gatling-app" % gatling_version),
"io.gatling" % "gatling-core" % gatling_version),
"io.gatling" % "gatling-commons" % gatling_version),
"io.gatling" % "gatling-recorder" % gatling_version),
"io.gatling" % "gatling-charts" % gatling_version),
"io.gatling.highcharts" % "gatling-charts-highcharts" % gatling_version),
"io.gatling" % "gatling-http" % gatling_version),
"io.gatling" % "gatling-http-client" % gatling_version),
"io.reactivex.rxjava2" % "rxjava" % "2.2.1",

"javax.annotation" % "javax.annotation-api" % "1.2",
"javax.ws.rs" % "javax.ws.rs-api" % "2.1",
"junit" % "junit" % "4.12",
"junit" % "junit-dep" % "4.10",
"org.codehaus.janino" % "janino" % "3.1.4",
"org.apache.commons" % "commons-lang3" % "3.9",
"org.apache.commons" % "commons-text" % "1.4",
"org.awaitility" % "awaitility" % "3.1.6",
"org.checkerframework" % "checker" % "2.5.4",
"org.flywaydb" % "flyway-core" % "7.13.0",
"org.freemarker" % "freemarker-gae" % "2.3.28",

"org.jline" % "jline-reader" % "3.7.1",
"org.junit.jupiter" % "junit-jupiter-api" % "5.0.0",
"org.junit.jupiter" % "junit-jupiter-engine" % "5.0.0",
"org.junit.platform" % "junit-platform-engine" % "1.0.0",
"org.junit.platform" % "junit-platform-runner" % "1.0.0",
"org.mockito" % "mockito-core" % "3.6.28",
"org.mockito" % "mockito-inline" % "3.6.28",
"org.mockito" % "mockito-scala" %% "1.16.3"
"org.pcollections" % "pcollections" % "2.1.3",
"org.postgresql" % "postgresql" % "42.2.18",
"org.reactivestreams" % "reactive-streams" % "1.0.2",
"org.reactivestreams" % "reactive-streams-tck" % "1.0.2",
"org.reflections" % "reflections" % "0.9.12",
"org.sangria-graphql" % "sangria" %% "2.0.1"
"org.sangria-graphql" % "sangria-spray-json" %% "1.0.2"


"org.scala-lang.modules" % "scala-parallel-collections" %% "1.0.0"
"org.scalameta" % "munit" %% "0.7.26"

"org.scalatestplus" % "selenium-3-141" %% "3.2.9.0"
"org.scalatestplus" % "testng-6-7" %% "3.2.9.0"


"org.seleniumhq.selenium" % "selenium-java" % "3.12.0",

"org.slf4j" % "slf4j-simple" % "1.7.26",
"org.typelevel" % "kind-projector" %%% "0.13.0"
"org.tpolecat" % "doobie-core" %% "0.13.4"
"org.tpolecat" % "doobie-hikari" %% "0.13.4"
"org.tpolecat" % "doobie-postgres" %% "0.13.4"

"org.wartremover" % "wartremover" %%% "2.4.16"
"org.xerial" % "sqlite-jdbc" % "3.36.0.1",
*/