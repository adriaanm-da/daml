lazy val root = RootProject(file("../"))
lazy val nameof = LocalProject("nameof")
lazy val `scala-utils` = LocalProject("scala-utils")
lazy val `logging-entries` = LocalProject("logging-entries")
lazy val `scalatest-utils` = LocalProject("scalatest-utils")

lazy val `cli-opts` = project
lazy val `db-backend` = project
lazy val `fetch-contracts` = project
lazy val `http-json` = project
lazy val `http-json-cli` = project
lazy val `http-json-ledger-client` = project
lazy val `http-json-oracle` = project
lazy val `http-json-perf` = project
lazy val `http-json-testing` = project
lazy val `jwt` = project.settings(
        libraryDependencies ++= Seq(
            Deps.com_github_scopt_scopt,
            Deps.com_typesafe_scala_logging_scala_logging,
            Deps.org_scalaz_scalaz_core,
            Deps.com_auth0_java_jwt,
            Deps.com_auth0_jwks_rsa,
            Deps.com_google_guava_guava,
            Deps.org_slf4j_slf4j_api)
    )
lazy val `lf-value-json` = project
lazy val `utils` = project