package com.github.georgeii.phrasehunter.services

import cats.Applicative
import cats.effect.{ MonadCancel, Resource }
import cats.implicits._
//import cats.syntax.traverse._
import com.github.georgeii.phrasehunter.models.phrase._
import com.github.georgeii.phrasehunter.models.{ Subtitle, SubtitleOccurrenceDetails }

import java.io.File
import scala.io.{ BufferedSource, Source }

trait Subtitles[F[_]] {
  def findAll(phrase: Phrase): F[List[SubtitleOccurrenceDetails]]
}

object Subtitles {
  def make[F[_]](
      subtitleStorage: List[File]
  )(
      implicit F: MonadCancel[F, Throwable]
  ): Subtitles[F] = {
    new Subtitles[F] {
      override def findAll(phrase: Phrase): F[List[SubtitleOccurrenceDetails]] = {
        subtitleStorage
          .map { file =>
            val fileResource = makeReadFileResource[F](file)

            fileResource.use { bufferedSource =>
              findOccurrencesInParticularFile(phrase, bufferedSource.mkString, file.getAbsolutePath).pure[F]
            }
          }
          .sequence
          .map(_.flatten)
      }
    }
  }

//  def getSubtitlesWithPhraseInAllFiles(
//      phrase: String,
//      directory: String = directory
//  ): F[List[SubtitleOccurrenceDetails]] = {
//    for {
//      vectorOfFiles <- getAllSubtitleFilesInDirectory(directory)
//      files         <- vectorOfFiles.traverse(file => findPhraseInFile(phrase, file))
//    } yield files.flatten
//  }

  def getAllSubtitleFilesInDirectory[F[_]: Applicative](directory: String): F[List[File]] = {
    val filesDirectory = new File(directory)

    if (filesDirectory.exists && filesDirectory.isDirectory) {
      filesDirectory.listFiles.filter(_.isFile).toList.pure[F]
    } else {
      List.empty[File].pure[F]
    }
  }

  private def makeReadFileResource[F[_]: Applicative](
      file: File
  ): Resource[F, BufferedSource] = {
    Resource.make {
      Source.fromFile(file).pure[F]
    } { inBufferedSource: BufferedSource =>
      inBufferedSource.close().pure[F]
    }
  }

  private def findOccurrencesInParticularFile(
      phrase: Phrase,
      source: String,
      fileAbsPath: String
  ): List[SubtitleOccurrenceDetails] = {
    val splitSourceBySeparateSubtitle = source.split("\r\n\r\n").toList

    val parsedSubtitles = for {
      separateSubtitleInFile <- splitSourceBySeparateSubtitle
      subtitleParts = separateSubtitleInFile.split("\r\n")
    } yield Subtitle(subtitleParts(0).toInt, subtitleParts(1), subtitleParts.drop(2).mkString(" "))

    val subtitlesThatContainPhrase = parsedSubtitles.filter(_.text.contains(phrase))

    subtitlesThatContainPhrase.map { subtitle =>
      val (startString, endString) = extractTimestampsFromSubtitleString(subtitle.timestamp)
      val startMillis              = convertStringTimeToMillis(startString)
      val endMillis                = convertStringTimeToMillis(endString)

      SubtitleOccurrenceDetails(fileAbsPath, subtitle.number, startMillis, endMillis, subtitle.text)
    }
  }

  private def extractTimestampsFromSubtitleString(timeString: String): (String, String) = {
    // 00:02:19,482 --> 00:02:21,609
    val startAndEndTimestamps = timeString.split(" --> ")

    (startAndEndTimestamps(0), startAndEndTimestamps(1))
  }

  private def convertStringTimeToMillis(timestamp: String): Long = {
    // 00:02:19,482
    val hoursMinutesSecondsSplit = timestamp.split(":")
    val secondsMillisSplit       = hoursMinutesSecondsSplit(2).split(",")

    val hours   = hoursMinutesSecondsSplit(0).toLong
    val minutes = hoursMinutesSecondsSplit(1).toLong
    val seconds = secondsMillisSplit(0).toLong
    val millis  = secondsMillisSplit(1).toLong

    hours * 60 * 60 * 1000 + minutes * 60 * 1000 + seconds * 1000 + millis
  }
}
