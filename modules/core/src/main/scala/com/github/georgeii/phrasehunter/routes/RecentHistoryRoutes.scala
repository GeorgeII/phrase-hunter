package com.github.georgeii.phrasehunter.routes

import cats.effect.Concurrent
import com.github.georgeii.phrasehunter.codecs.phraseRecord._
import com.github.georgeii.phrasehunter.services.RecentHistory
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import org.http4s.circe.CirceEntityCodec._

final case class RecentHistoryRoutes[F[_]: Concurrent](
    history: RecentHistory[F]
) extends Http4sDsl[F] {

  private[routes] val prefixPath = "/history"

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root =>
      Ok(history.getAllRecent())

  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )
}
