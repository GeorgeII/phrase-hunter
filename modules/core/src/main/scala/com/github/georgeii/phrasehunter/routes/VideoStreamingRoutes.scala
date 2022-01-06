package com.github.georgeii.phrasehunter.routes

import cats.data.NonEmptyList
import cats.effect.Async
import cats.effect.kernel.Concurrent
import cats.implicits.toFunctorOps
import fs2.io.file.{ Files, Path => fs2Path }
import org.http4s.{ CacheDirective, Header, HttpRoutes, MediaType, StaticFile, Status, TransferCoding }
import org.http4s.headers._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.Range.SubRange
import org.http4s.server.Router
import org.typelevel.ci.CIString

import java.io.{ File, RandomAccessFile }

final case class VideoStreamingRoutes[F[_]: Async: Concurrent]() extends Http4sDsl[F] {

  private[routes] val prefixPath = "/videos"

  object OptionalVideoStartTimeParameterMatcher extends OptionalQueryParamDecoderMatcher[Long]("startFrom")

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {

    case request @ GET -> Root / videoFileName :? OptionalVideoStartTimeParameterMatcher(startFrom) =>
      val filePath   = "./data/videos/" + videoFileName.replaceAll("%20", " ")
      val file       = new File(filePath)
      val fileLength = file.length()

      val rangeHeader: Option[NonEmptyList[Header.Raw]] = request.headers.get(CIString("Range"))
      val range: Option[Array[String]]                  = rangeHeader.map(_.head.value.split("=")(1).split("-"))

      val start: Long = range.map(_.head.toLong).getOrElse(0L)
      val end: Long   = range.map(ran => if (ran.length > 1) ran(1).toLong else fileLength).getOrElse(fileLength)

      println(start)
      println(end)

      val chunkSize = 1024 * 64

      val fileStream = Files[F].readRange(
        path = fs2Path(filePath),
        chunkSize = chunkSize,
        start = start,
        end = end
      )

      Ok(fileStream)
        .map(_.withStatus(Status.PartialContent))
        .map(response =>
          response.putHeaders(
            `Content-Type`(MediaType.video.mp4),
            `Content-Length`(end - start),
            `Content-Range`(SubRange(start, end), Option(file.length)),
            `Accept-Ranges`.bytes,
            `Cache-Control`(CacheDirective.`no-store`)
          )
        )
  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )
}
