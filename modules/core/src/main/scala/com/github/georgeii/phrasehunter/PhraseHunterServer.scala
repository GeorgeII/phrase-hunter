package com.github.georgeii.phrasehunter

import cats.effect.{ Async, Resource }
import cats.syntax.all._
import com.comcast.ip4s._
import com.github.georgeii.phrasehunter.routes.SearchRoutes
import com.github.georgeii.phrasehunter.services.Subtitles
import fs2.Stream
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits._
import org.http4s.server.middleware.Logger

import java.io.File

object PhraseHunterServer {

  def stream[F[_]: Async]: Stream[F, Nothing] = {
    for {
      client <- Stream.resource(EmberClientBuilder.default[F].build)
      helloWorldAlg = HelloWorld.impl[F]
      jokeAlg       = Jokes.impl[F](client)

      // \/\/\/\/\/ TODO: Fix this ↓↓↓↓↓

      subtitleDirectory = "data/subtitles/"
//      searchServiceF    = Subtitles.getAllSubtitleFilesInDirectory[F](subtitleDirectory)
//      searchRoutes      = searchServiceF.map(files => SearchRoutes(Subtitles.make(files)))
      filesDirectory = new File(subtitleDirectory)

      files = if (filesDirectory.exists && filesDirectory.isDirectory) {
        filesDirectory.listFiles.filter(_.isFile).toList
      } else {
        List.empty[File]
      }
      // \/\/\/\/\/ TODO: Fix this ↑↑↑↑↑

      httpApp = (
        SearchRoutes(Subtitles.make(files)).routes

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
