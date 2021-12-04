import sbt._

object Dependencies {

  object V {
    val cats            = "2.6.1"
    val catsEffect      = "3.2.9"
    val fs2             = "3.1.3"
    val http4s          = "0.23.6"
    val circe           = "0.14.1"
    val newtype         = "0.4.4"
    val refined         = "0.9.27"
    val derevo          = "0.12.8"
    val munit           = "0.7.20"
    val munitCatsEffect = "0.13.0"
    val pureConfig      = "0.16.0"
    val ciris           = "2.1.1"
    val log4cats        = "2.1.1"

    val betterMonadicFor = "0.3.1"
    val kindProjector    = "0.13.2"
    val logback          = "1.2.7"
    val organizeImports  = "0.6.0"
    val semanticDB       = "4.4.30"
  }

  object Libraries {
    def circe(artifact: String): ModuleID  = "io.circe"   %% s"circe-$artifact"  % V.circe
    def ciris(artifact: String): ModuleID  = "is.cir"     %% artifact            % V.ciris
    def http4s(artifact: String): ModuleID = "org.http4s" %% s"http4s-$artifact" % V.http4s
    def derevo(artifact: String): ModuleID = "tf.tofu"    %% s"derevo-$artifact" % V.derevo

    val cats       = "org.typelevel" %% "cats-core"   % V.cats
    val catsEffect = "org.typelevel" %% "cats-effect" % V.catsEffect

    val fs2 = "co.fs2" %% "fs2-core" % V.fs2

    val circeCore    = circe("core")
    val circeGeneric = circe("generic")
    val circeParser  = circe("parser")
    val circeRefined = circe("refined")

    val http4sDsl    = http4s("dsl")
    val http4sServer = http4s("ember-server")
    val http4sClient = http4s("ember-client")
    val http4sCirce  = http4s("circe")

    val cirisCore    = ciris("ciris")
    val cirisEnum    = ciris("ciris-enumeratum")
    val cirisRefined = ciris("ciris-refined")

    val derevoCore  = derevo("core")
    val derevoCats  = derevo("cats")
    val derevoCirce = derevo("circe-magnolia")

    val newtype = "io.estatico" %% "newtype" % V.newtype

    val refinedCore = "eu.timepit" %% "refined"      % V.refined
    val refinedCats = "eu.timepit" %% "refined-cats" % V.refined

    val log4cats = "org.typelevel" %% "log4cats-slf4j" % V.log4cats

    // Runtime
    val logback = "ch.qos.logback" % "logback-classic" % V.logback

    // Test
    val munit           = "org.scalameta" %% "munit"               % V.munit           % Test
    val munitCatsEffect = "org.typelevel" %% "munit-cats-effect-3" % V.munitCatsEffect % Test

    // Scalafix rules
    val organizeImports = "com.github.liancheng" %% "organize-imports" % V.organizeImports
  }

  object CompilerPlugin {
    val betterMonadicFor = compilerPlugin(
      "com.olegpy" %% "better-monadic-for" % V.betterMonadicFor
    )
    val kindProjector = compilerPlugin(
      "org.typelevel" % "kind-projector" % V.kindProjector cross CrossVersion.full
    )
    val semanticDB = compilerPlugin(
      "org.scalameta" % "semanticdb-scalac" % V.semanticDB cross CrossVersion.full
    )
  }

}
