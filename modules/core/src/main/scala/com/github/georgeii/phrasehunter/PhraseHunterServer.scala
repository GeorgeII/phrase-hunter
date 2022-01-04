package com.github.georgeii.phrasehunter

import cats.effect.{ Async, Resource }
import cats.syntax.all._
import com.comcast.ip4s._
import fs2.Stream
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits._
import org.http4s.server.middleware.{ Logger => MiddlewareLogger }
import com.github.georgeii.phrasehunter.routes.{ RecentHistoryRoutes, SearchRoutes }
import com.github.georgeii.phrasehunter.services.{ RecentHistory, Subtitles }
import com.github.georgeii.phrasehunter.util.{ FileReader, StaticResourceResolver }
import dev.profunktor.redis4cats.effect.Log.Stdout.instance
import dev.profunktor.redis4cats.{ Redis, RedisCommands }
import doobie.Transactor
import doobie.util.transactor.Transactor.Aux
import org.http4s.server.Router
import org.typelevel.log4cats.Logger

object PhraseHunterServer {

  def stream[F[_]: Async: Logger]: Stream[F, Nothing] = {
    val subtitleDirectory = StaticResourceResolver.getSubtitleDirectoryPath
    val subtitleFiles = for {
      sd    <- subtitleDirectory
      files <- FileReader.getAllFilesInDirectory(sd)
    } yield files

    val postgresTransactor: Aux[F, Unit] = Transactor.fromDriverManager[F](
      "org.postgresql.Driver",                              // driver classname
      "jdbc:postgresql://postgres_container/phrase-hunter", // connect URL (driver-specific)
      "postgres",                                           // user
      "password"                                            // password
    )

    val redis: Resource[F, RedisCommands[F, String, String]] = Redis[F].utf8("redis://redis")

    for {
      client <- Stream.resource(EmberClientBuilder.default[F].build)

      publicRoutes = SearchRoutes(Subtitles.make(postgresTransactor, redis, subtitleFiles)).routes <+>
          RecentHistoryRoutes(RecentHistory.make(postgresTransactor, redis)).routes

      httpRoutes = Router(
        routes.version.v1 -> publicRoutes
      ).orNotFound

      // With Middlewares in place
      finalHttpApp = MiddlewareLogger.httpApp(logHeaders = true, logBody = true)(httpRoutes)

      exitCode <- Stream.resource(
        EmberServerBuilder
          .default[F]
          .withHost(ipv4"0.0.0.0")
          .withPort(port"8080")
          .withHttpApp(finalHttpApp)
          .build >>
            Resource.eval(Async[F].never)
      )
    } yield exitCode
  }.drain
}
