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
      println(videoFileName)
      println(videoFileName.replaceAll("%20", " "))
      val filePath   = "./data/videos/" + videoFileName.replaceAll("%20", " ")
      val file       = new File(filePath)
      val fileLength = file.length()
      println(file.getPath)
      println(file.getAbsolutePath)
      println(file.canRead)
      println(fileLength)

      val rangeHeader: Option[NonEmptyList[Header.Raw]] = request.headers.get(CIString("Range"))
      println(s"RangeHeader is: $rangeHeader")
      val range = rangeHeader.map(_.head.value.split("=")(1).split("-"))
      println(s"Range is: $range")
      val start = range.map(_.head.toLong).getOrElse(0L)
      val end   = range.map(ran => if (ran.length > 1) ran(1).toLong else fileLength).getOrElse(fileLength)

      println(start)
      println(end)

      val chunkSize = 1024 * 64

      val fileStream = Files[F].readRange(
        path = fs2Path(filePath),
        chunkSize = chunkSize,
        start = start,
        end = end
      )

      // 25mb
      val maxBytesToSend = 26_214_400
      val anotherEnd     = if (file.length - start > maxBytesToSend) start + maxBytesToSend else file.length

      val buffer = if (file.length - start > maxBytesToSend) maxBytesToSend.toLong else file.length - start

      val bytes = Array.ofDim[Byte](buffer.toInt)

      val raf = new RandomAccessFile(filePath, "r")
      raf.seek(start)
      val bytesRead = raf.read(bytes, 0, buffer.toInt)
      val readChunk = bytes.take(bytesRead)

//      new RandomAccessFile(filePath, "r").read(bytes, start.toInt, maxBytesToSend)

//      val inputStream: InputStream = scala.io.Source.fromResource(filePath).openStream()

      Ok(readChunk)
        .map(_.withStatus(Status.PartialContent))
        .map(response =>
          response.putHeaders(
            `Content-Type`(MediaType.video.mp4),
            `Content-Length`(anotherEnd - start),
            `Content-Range`(SubRange(start, anotherEnd), Option(file.length)),
            `Accept-Ranges`.bytes,
            `Cache-Control`(CacheDirective.`no-store`)
          )
        )

//      StaticFile
//        .fromPath(
//          f = fs2Path(filePath),
//          start = start,
//          end = end,
//          buffsize = chunkSize,
//          Option(request),
//          StaticFile.calculateETag
//        )
//        .map(_.withStatus(Status.PartialContent))
//        .map(response => response.putHeaders(`Content-Range`(SubRange(start, end), Option(fileLength - 1)), `Accept-Ranges`.bytes))
//        .getOrElseF(NotFound("Such file does not exist"))

  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )
}
