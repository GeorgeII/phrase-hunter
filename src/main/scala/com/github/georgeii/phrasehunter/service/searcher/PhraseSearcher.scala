package com.github.georgeii.phrasehunter.service.searcher

import cats.effect.{IO, Resource}

import java.io.File
import scala.io.{BufferedSource, Source}

case class SubtitleOccurrenceDetails(filename: File, phraseId: Int, startTimeMillis: Long, endTimeMillis: Long)
case class Subtitle(number: Int, timestamp: String, text: String)


class SubtitleSearcher {

  def getSubtitlesWithPhrase(phrase: String): Vector[SubtitleOccurrenceDetails] = {
    val allSubtitleFilesIO: IO[Vector[File]] = getAllSubtitleFiles()

    val filesWithOccurrences = for {
        subtitleFiles <- allSubtitleFilesIO
        file          <- subtitleFiles
        indices       <- getIndicesOf(file)
      } yield SubtitleOccurrenceDetails(file, indices)

    filesWithOccurrences
    ???
  }

  def getAllSubtitleFiles(): IO[Vector[File]] = {???}

  def getIndicesOf(file: File): IO[Vector[Int]] = {???}

  def makeReadFileResource(file: File): Resource[IO, BufferedSource] = {
    Resource.make {
      IO(Source.fromFile(file))                                  // build
    } { inBufferedSource =>
      IO(inBufferedSource.close()).handleErrorWith(_ => IO.unit) // release
    }
  }

  def extractTimestampsFromSubtitleString(timeString: String): (String, String) = {
    // 00:02:19,482 --> 00:02:21,609
    val startAndEndTimestamps = timeString.split(" --> ")

    (startAndEndTimestamps(0), startAndEndTimestamps(1))
  }

  def convertStringTimeToMillis(timestamp: String): Long = {
    // 00:02:19,482
    val hoursMinutesSecondsSplit = timestamp.split(":")
    val secondsMillisSplit       = hoursMinutesSecondsSplit(2).split(",")

    val hours   = hoursMinutesSecondsSplit(0).toLong
    val minutes = hoursMinutesSecondsSplit(1).toLong
    val seconds = secondsMillisSplit(0).toLong
    val millis  = secondsMillisSplit(1).toLong

    hours * 60 * 60 * 1000 + minutes * 60 * 1000 + seconds * 1000 + millis
  }

  def parse

  def findOccurrences(pattern: String, source: String): IO[Vector[SubtitleOccurrenceDetails]] = {
    val splitSourceBySeparateSubtitle = source.split("\n\n").toList

    val parsedSubtitles = for {
      separateSubtitle <- splitSourceBySeparateSubtitle
      subtitleParts = separateSubtitle.split("\n")
    } yield Subtitle(subtitleParts(0).toInt, subtitleParts(1), subtitleParts.drop(2).mkString(" "))

    parsedSubtitles
      .map { subtitle =>
        val (startString, endString) = extractTimestampsFromSubtitleString(subtitle.timestamp)
        val startMillis = convertStringTimeToMillis(startString)
        val endMillis   = convertStringTimeToMillis(endString)

        SubtitleOccurrenceDetails(null, subtitle.number, startMillis, endMillis)
      }

    

  }

  def findPhraseInFile(phrase: String, file: File): IO[Vector[SubtitleOccurrenceDetails]] = {
    val fileResource = makeReadFileResource(file)

    fileResource.use { fileBufferedSource =>
      findOccurrences(phrase, fileBufferedSource.mkString)
    }
  }

}
