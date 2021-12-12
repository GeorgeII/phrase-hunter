package com.github.georgeii.phrasehunter.util

import cats.Applicative
import cats.effect.{ Resource, Sync }

import java.io.File
import scala.io.{ BufferedSource, Source }

object FileReader {

  def getAllFilesInDirectory[F[_]: Applicative](directory: String): F[List[File]] = {
    val filesDirectory = new File(directory)

    Applicative[F].pure {
      if (filesDirectory.exists && filesDirectory.isDirectory) filesDirectory.listFiles.filter(_.isFile).toList
      else List.empty[File]
    }
  }

  def getNameNoExtension(file: File): String =
    file.getName.split('.').toList.dropRight(1).mkString(".")

  def makeSubtitleFileResource[F[_]: Sync](file: File): Resource[F, BufferedSource] =
    Resource.fromAutoCloseable { Applicative[F].pure(Source.fromFile(file)) }

}
