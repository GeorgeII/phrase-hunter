package com.github.georgeii.phrasehunter.programs.util

import cats.Applicative
import cats.effect.{ Resource, Sync }
import cats.implicits._

import java.io.File
import scala.io.{ BufferedSource, Source }

object FileReader {

  def makeFileResource[F[_]: Sync](
      file: File
  ): Resource[F, BufferedSource] = {
    Resource.fromAutoCloseable {
      Applicative[F].pure(Source.fromFile(file))
    }
  }

  def getAllFilesInDirectory[F[_]: Applicative](directory: String): F[List[File]] = {
    val filesDirectory = new File(directory)

    if (filesDirectory.exists && filesDirectory.isDirectory) {
      filesDirectory.listFiles.filter(_.isFile).toList.pure[F]
    } else {
      List.empty[File].pure[F]
    }
  }

}
