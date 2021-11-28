import Dependencies._

ThisBuild / scalaVersion := "2.13.5"
ThisBuild / version := "0.0.1"
ThisBuild / organization := "com.github.georgeii"
ThisBuild / organizationName := "GeorgeII"

ThisBuild / evictionErrorLevel := Level.Warn
ThisBuild / scalafixDependencies += Libraries.organizeImports

resolvers += Resolver.sonatypeRepo("snapshots")

val scalafixCommonSettings = inConfig(IntegrationTest)(scalafixConfigSettings(IntegrationTest))

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
    scalafixCommonSettings,
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
    scalafixCommonSettings,
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
      Libraries.newtype,
      Libraries.refinedCats,
      Libraries.refinedCore,
      Libraries.log4cats,
      Libraries.logback % Runtime
    )
  )

addCommandAlias("runLinter", ";scalafixAll --rules OrganizeImports")

//val Http4sVersion          = "0.21.24"
//val CirceVersion           = "0.13.0"
//val MunitVersion           = "0.7.20"
//val LogbackVersion         = "1.2.3"
//val MunitCatsEffectVersion = "0.13.0"
//
//val PureConfigVersion = "0.16.0"
//val CirisVersion      = "2.1.1"
//val log4catsVersion   = "2.1.1"
//
//lazy val root = (project in file("."))
//  .settings(
//    organization := "com.github.georgeii",
//    name := "phrase-hunter",
//    version := "0.0.1-SNAPSHOT",
//    scalaVersion := "2.13.6",
//    libraryDependencies ++= Seq(
//      "org.http4s"     %% "http4s-blaze-server" % Http4sVersion,
//      "org.http4s"     %% "http4s-blaze-client" % Http4sVersion,
//      "org.http4s"     %% "http4s-circe"        % Http4sVersion,
//      "org.http4s"     %% "http4s-dsl"          % Http4sVersion,
//      "io.circe"       %% "circe-generic"       % CirceVersion,
//      "org.scalameta"  %% "munit"               % MunitVersion % Test,
//      "org.typelevel"  %% "munit-cats-effect-2" % MunitCatsEffectVersion % Test,
//      "ch.qos.logback" % "logback-classic"      % LogbackVersion,
//      "org.typelevel"  %% "log4cats-slf4j"      % log4catsVersion,
//      "is.cir"         %% "ciris"               % CirisVersion
//    ),
//    addCompilerPlugin("org.typelevel" %% "kind-projector"     % "0.13.0" cross CrossVersion.full),
//    addCompilerPlugin("com.olegpy"    %% "better-monadic-for" % "0.3.1"),
//    testFrameworks += new TestFramework("munit.Framework")
//  )
