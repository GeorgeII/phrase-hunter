package com.github.georgeii.phrasehunter

import cats.effect.{ Async, Resource }
import cats.syntax.all._
import com.comcast.ip4s._
import fs2.Stream
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits._
import org.http4s.server.middleware.Logger
import com.github.georgeii.phrasehunter.routes.{ RecentHistoryRoutes, SearchRoutes }
import com.github.georgeii.phrasehunter.services.{ RecentHistory, Subtitles }
import com.github.georgeii.phrasehunter.util.FileReader
import doobie.Transactor
import doobie.util.transactor.Transactor.Aux

object PhraseHunterServer {

  def stream[F[_]: Async]: Stream[F, Nothing] = {
    val subtitleDirectory = "data/subtitles/"
    val subtitleFiles     = FileReader.getAllFilesInDirectory(subtitleDirectory)

    val postgresTransactor: Aux[F, Unit] = Transactor.fromDriverManager[F](
      "org.postgresql.Driver",         // driver classname
      "jdbc:postgresql:phrase-hunter", // connect URL (driver-specific)
      "postgres",                      // user
      "password"                       // password
    )

    val redis: Resource[F, _] = Resource.never

    for {
      client <- Stream.resource(EmberClientBuilder.default[F].build)

      httpApp = (
        SearchRoutes(Subtitles.make(postgresTransactor, subtitleFiles)).routes
          <+> RecentHistoryRoutes(RecentHistory.make(postgresTransactor, redis)).routes
      ).orNotFound

      // With Middlewares in place
      finalHttpApp = Logger.httpApp(logHeaders = true, logBody = true)(httpApp)

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
