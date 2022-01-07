package com.github.georgeii.phrasehunter

import cats.effect.{ Async, Resource }
import cats.syntax.all._
import com.github.georgeii.phrasehunter.config.AppConfig.HttpServerConfig
import fs2.Stream
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits._
import org.http4s.server.middleware.{ Logger => MiddlewareLogger }
import com.github.georgeii.phrasehunter.routes.{ IndexRoutes, RecentHistoryRoutes, SearchRoutes, VideoStreamingRoutes }
import com.github.georgeii.phrasehunter.services.{ RecentHistory, Subtitles, VideoStreaming }
import org.http4s.server.Router
import org.typelevel.log4cats.Logger

object PhraseHunterServer {

  def stream[F[_]: Async: Logger](
      cfg: HttpServerConfig,
      subtitlesService: Subtitles[F],
      recentHistoryService: RecentHistory[F],
      videoService: VideoStreaming[F]
  ): Stream[F, Nothing] = {

    val publicRoutes = SearchRoutes(subtitlesService).routes <+>
          RecentHistoryRoutes(recentHistoryService).routes <+>
          VideoStreamingRoutes(videoService).routes

    val staticPages = IndexRoutes().routes

    val httpRoutes = Router(
      "/"               -> staticPages,
      routes.version.v1 -> publicRoutes
    ).orNotFound

    for {
      client <- Stream.resource(EmberClientBuilder.default[F].build)

      // With Middlewares in place
      finalHttpApp = MiddlewareLogger.httpApp(logHeaders = true, logBody = true)(httpRoutes)

      exitCode <- Stream.resource(
        EmberServerBuilder
          .default[F]
          .withHost(cfg.host)
          .withPort(cfg.port)
          .withHttpApp(finalHttpApp)
          .build >>
            Resource.eval(Async[F].never)
      )
    } yield exitCode
  }.drain
}
