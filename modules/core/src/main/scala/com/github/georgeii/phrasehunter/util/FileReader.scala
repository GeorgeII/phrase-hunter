package com.github.georgeii.phrasehunter.util

import cats.Applicative
import cats.effect.{ Resource, Sync }
import com.github.georgeii.phrasehunter.models.phrase.Phrase
import com.github.georgeii.phrasehunter.models.{ Subtitle, SubtitleOccurrenceDetails }

import java.io.File
import scala.io.{ BufferedSource, Source }
import scala.util.Try

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

  def extractInfoFromSubtitle(subtitle: String): Either[Throwable, Subtitle] = {
    val metaInfo = subtitle.split("\r\n")

    Try {
      Subtitle(metaInfo(0).toInt, metaInfo(1), metaInfo.drop(2).mkString(" "))
    }.toEither
  }

  def findOccurrencesInFile(
      phrase: Phrase,
      text: String,
      filename: String
  ): List[SubtitleOccurrenceDetails] = {
    val separateSubtitles = text.split("\r\n\r\n")

    val subtitlesThatContainPhrase = separateSubtitles.view
      .map(sub => extractInfoFromSubtitle(sub))
      .filter(_.isRight)
      .map(_.toOption.get)
      .filter(_.text.toLowerCase.contains(phrase.toString.toLowerCase))
      .map { sub =>
        val (startMillis, endMillis) = TimeConverter.extractStartEndTimestamps(sub.timestamp)

        SubtitleOccurrenceDetails(filename, sub.number, startMillis, endMillis, sub.text)
      }
      .toList

    subtitlesThatContainPhrase
  }

}
