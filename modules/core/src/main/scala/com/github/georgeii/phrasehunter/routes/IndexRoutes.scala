package com.github.georgeii.phrasehunter.routes

import cats.effect.kernel.Concurrent
import fs2.io.file.{ Path => fs2Path }
import cats.effect.Async
import org.http4s.{ HttpRoutes, StaticFile }
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

case class IndexRoutes[F[_]: Async: Concurrent](
    assetsDirectory: String
) extends Http4sDsl[F] {

  private[routes] val prefixPath = "/"

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case request @ GET -> Root =>
      StaticFile
        .fromPath(fs2Path(assetsDirectory + "index.html"), Option(request))
        .getOrElseF(NotFound()) // In case the file doesn't exist

  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )
}
