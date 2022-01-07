package com.github.georgeii.phrasehunter.routes

import cats.Applicative
import cats.data.{ NonEmptyList, OptionT }
import cats.effect.Async
import cats.implicits._
import cats.effect.kernel.Concurrent
import cats.implicits.toFunctorOps
import com.github.georgeii.phrasehunter.services.VideoStreaming
import org.http4s.{ CacheDirective, Header, HttpRoutes, MediaType, Status }
import org.http4s.headers._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.Range.SubRange
import org.http4s.server.Router
import org.typelevel.ci.CIString

final case class VideoStreamingRoutes[F[_]: Async: Concurrent: Applicative](
    videos: VideoStreaming[F]
) extends Http4sDsl[F] {

  private[routes] val prefixPath = "/videos"

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {

    case request @ GET -> Root / videoFileName =>
      val rangeHeaders: Option[NonEmptyList[Header.Raw]] = request.headers.get(CIString("Range"))

      val maybeVideoChunk = for {
        rangeHead  <- rangeHeaders.map(_.head.value)
        videoChunk <- Option(videos.getVideoChunk(videoFileName, rangeHead))
      } yield videoChunk

      val maybeSuccessResponse = for {
        videoChunk <- OptionT(maybeVideoChunk.sequence)
      } yield {
        Ok(videoChunk.bytes)
          .map(_.withStatus(Status.PartialContent))
          .map(response =>
            response.putHeaders(
              `Content-Type`(MediaType.video.mp4),
              `Content-Length`(videoChunk.endPos - videoChunk.startPos),
              `Content-Range`(SubRange(videoChunk.startPos, videoChunk.endPos), Option(videoChunk.fileLength)),
              `Accept-Ranges`.bytes,
              `Cache-Control`(CacheDirective.`no-store`)
            )
          )
      }

      maybeSuccessResponse.getOrElse(BadRequest("No header 'Range' was passed.")).flatten

  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )
}
