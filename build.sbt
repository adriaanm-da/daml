ThisBuild / organization := "com.daml"
ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.13.7"

lazy val root = project.
  in(file(".")).
  settings(publishArtifact := false).
  aggregate(`daml-lf`,
            `libs-scala`)

lazy val `daml-lf` = project
lazy val `libs-scala` = project
lazy val ledger = project
lazy val `ledger-api` = project
lazy val `ledger-service` = project
