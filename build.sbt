import Dependencies._

ThisBuild / scalaVersion := "2.13.5"
ThisBuild / version := "0.0.1"
ThisBuild / organization := "com.github.georgeii"
ThisBuild / organizationName := "GeorgeII"

ThisBuild / evictionErrorLevel := Level.Warn

resolvers += Resolver.sonatypeRepo("snapshots")

lazy val root = (project in file("."))
  .settings(
    name := "phrase-hunter"
  )
  .aggregate(core, tests)

lazy val tests = (project in file("modules/tests"))
  .configs(IntegrationTest)
  .settings(
    name := "phrase-hunter-test-suite",
    scalacOptions ++= List("-Ymacro-annotations", "-Yrangepos", "-Wconf:cat=unused:info"),
    testFrameworks += new TestFramework("munit.Framework"),
    Defaults.itSettings,
    libraryDependencies ++= Seq(
          CompilerPlugin.kindProjector,
          CompilerPlugin.betterMonadicFor,
          CompilerPlugin.semanticDB,
          Libraries.munit,
          Libraries.munitCatsEffect
        )
  )
  .dependsOn(core)

lazy val core = (project in file("modules/core"))
  .enablePlugins(DockerPlugin)
  .enablePlugins(AshScriptPlugin)
  .settings(
    name := "phrase-hunter-core",
    Docker / packageName := "phrase-hunter",
    scalacOptions ++= List("-Ymacro-annotations", "-Yrangepos", "-Wconf:cat=unused:info"),
    scalafmtOnCompile := true,
    resolvers += Resolver.sonatypeRepo("snapshots"),
    Defaults.itSettings,
    dockerBaseImage := "openjdk:11-jre-slim-buster",
    dockerExposedPorts ++= Seq(8080),
    makeBatScripts := Seq(),
    dockerUpdateLatest := true,
    libraryDependencies ++= Seq(
          CompilerPlugin.kindProjector,
          CompilerPlugin.betterMonadicFor,
          CompilerPlugin.semanticDB,
          Libraries.cats,
          Libraries.catsEffect,
          Libraries.circeCore,
          Libraries.circeGeneric,
          Libraries.circeParser,
          Libraries.circeRefined,
          Libraries.cirisCore,
          Libraries.cirisEnum,
          Libraries.cirisRefined,
          Libraries.fs2,
          Libraries.http4sDsl,
          Libraries.http4sServer,
          Libraries.http4sClient,
          Libraries.http4sCirce,
          Libraries.derevoCats,
          Libraries.derevoCore,
          Libraries.derevoCirce,
          Libraries.doobieCore,
          Libraries.doobiePostgres,
          Libraries.doobieSpecs2,
          Libraries.newtype,
          Libraries.refinedCats,
          Libraries.refinedCore,
          Libraries.log4cats,
          Libraries.logback % Runtime
        )
  )
