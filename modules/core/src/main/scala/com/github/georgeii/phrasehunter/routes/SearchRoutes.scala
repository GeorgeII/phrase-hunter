package com.github.georgeii.phrasehunter.routes

import com.github.georgeii.phrasehunter.models.phrase._
import com.github.georgeii.phrasehunter.codecs.phrase._
import com.github.georgeii.phrasehunter.codecs.subtitleOccurrenceDetails._
import com.github.georgeii.phrasehunter.services.Subtitles

import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.effect.kernel.Concurrent
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import org.http4s.circe.CirceEntityCodec._

final case class SearchRoutes[F[_]: Concurrent](
    subtitles: Subtitles[F]
) extends Http4sDsl[F] {

  private[routes] val prefixPath = "/search"

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root =>
      Ok("I'm fine!")
//      Ok(subtitles.findAll(Phrase("smth")))

    case req @ POST -> Root =>
      for {
        phrase         <- req.as[Phrase]
        foundSubtitles <- subtitles.findAll(phrase)
        resp           <- Ok(foundSubtitles)
      } yield resp
  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )
}
