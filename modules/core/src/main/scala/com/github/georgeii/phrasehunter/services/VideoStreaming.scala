package com.github.georgeii.phrasehunter.services

import cats.effect.Sync
import cats.implicits._
import org.typelevel.log4cats.Logger

import java.io.{ File, RandomAccessFile }

trait VideoStreaming[F[_]] {
  def getVideoChunk(filename: String, rangeHeader: String): F[VideoChunk]
}

object VideoStreaming {
  def make[F[_]: Sync: Logger](
      videoFilesDirectory: String,
      fileExtension: String,
      bufferSize: Int = 1024 * 1024 * 4
  ): VideoStreaming[F] = new VideoStreaming[F] {

    override def getVideoChunk(filename: String, rangeHeader: String): F[VideoChunk] = {
      val filePath = videoFilesDirectory + filename + fileExtension
      val file     = new File(filePath)

      val range = rangeHeader.split("=")(1).split("-")
      val start = range.head.toLong
      val bytes = Array.ofDim[Byte](bufferSize)

      val raf = new RandomAccessFile(filePath, "r")
      raf.seek(start)
      val bytesRead = raf.read(bytes, 0, bufferSize)
      val readChunk = bytes.take(bytesRead)

      for {
        rc     <- readChunk.pure[F]
        rcInfo <- VideoChunk(bytes = rc, startPos = start, endPos = start + rc.length, fileLength = file.length).pure[F]
        _ <- Logger[F].info(
          s"${rcInfo.bytes.length} bytes were read from the ${file.getAbsolutePath} file starting " +
              s"from ${rcInfo.startPos} byte up to ${rcInfo.endPos} byte."
        )
      } yield rcInfo

    }
  }

}

case class VideoChunk(bytes: Array[Byte], startPos: Long, endPos: Long, fileLength: Long)
