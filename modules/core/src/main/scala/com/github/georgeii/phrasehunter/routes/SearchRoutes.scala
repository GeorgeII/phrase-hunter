package com.github.georgeii.phrasehunter.routes

import cats.Monad
import com.github.georgeii.phrasehunter.models.phrase._
import com.github.georgeii.phrasehunter.services.Subtitles
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

final case class SearchRoutes[F[_]: Monad](
    subtitles: Subtitles[F]
) extends Http4sDsl[F] {

  private[routes] val prefixPath = "/search"

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root =>
      Ok("I'm fine!")
//      Ok(subtitles.findAll(Phrase("smth")))
  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )
}
