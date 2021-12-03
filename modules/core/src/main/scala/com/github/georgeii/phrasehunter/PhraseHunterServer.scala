package com.github.georgeii.phrasehunter

import cats.effect.{ Async, Resource }
import cats.syntax.all._
import com.comcast.ip4s._
import fs2.Stream
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits._
import org.http4s.server.middleware.Logger

import com.github.georgeii.phrasehunter.programs.util.FileReader
import com.github.georgeii.phrasehunter.routes.{ MediaRoutes, SearchRoutes }
import com.github.georgeii.phrasehunter.services.{ Media, Subtitles }

object PhraseHunterServer {

  def stream[F[_]: Async]: Stream[F, Nothing] = {
    for {
      client <- Stream.resource(EmberClientBuilder.default[F].build)
      helloWorldAlg = HelloWorld.impl[F]
      jokeAlg       = Jokes.impl[F](client)

      subtitleDirectory = "data/subtitles/"
      subtitleFiles     = FileReader.getAllFilesInDirectory(subtitleDirectory)
      mediaDirectory    = "data/media/"
      mediaFiles        = FileReader.getAllFilesInDirectory(mediaDirectory)

      httpApp = (
        SearchRoutes(Subtitles.make(subtitleFiles)).routes
          <+> MediaRoutes(Media.make(mediaFiles)).routes

//          <+> AsdfRoutes.jokeRoutes[F](jokeAlg)
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
