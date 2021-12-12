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

  object OptionalHistoryDepthParameterMatcher extends OptionalQueryParamDecoderMatcher[Int]("n")

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root :? OptionalHistoryDepthParameterMatcher(maybeN) =>
      val result = maybeN match {
        case Some(historyDepth) => history.getAllRecent(historyDepth)
        case None               => history.getAllRecent()
      }
      Ok(result)

    case GET -> Root / "found" :? OptionalHistoryDepthParameterMatcher(maybeN) =>
      val result = maybeN match {
        case Some(historyDepth) => history.getRecentFound(historyDepth)
        case None               => history.getRecentFound()
      }
      Ok(result)

    case GET -> Root / "notFound" :? OptionalHistoryDepthParameterMatcher(maybeN) =>
      val result = maybeN match {
        case Some(historyDepth) => history.getRecentNotFound(historyDepth)
        case None               => history.getRecentNotFound()
      }
      Ok(result)

  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )
}
