package com.github.georgeii.phrasehunter.service.searcher

import cats.effect.{IO, Resource}
import cats.implicits._

import java.io.File
import scala.io.{BufferedSource, Source}

case class Subtitle(number: Int, timestamp: String, text: String)
case class SubtitleOccurrenceDetails(
                                      fileAbsPath: String,
                                      phraseId: Int,
                                      startTimeMillis: Long,
                                      endTimeMillis: Long,
                                      text: String
                                    )

case class Config(subtitlesDirectory: String)


class SubtitleSearcher {

  val directory = "data/subtitles/"

  def getSubtitlesWithPhraseInAllFiles(
                                        phrase: String,
                                        directory: String = directory): IO[Vector[SubtitleOccurrenceDetails]] = {
    for {
      vectorOfFiles <- getVectorOfSubtitleFilesInDirectory(directory)
      files         <- vectorOfFiles.traverse(file => findPhraseInFile(phrase, file))
    } yield files.flatten
  }

  def getVectorOfSubtitleFilesInDirectory(directory: String = directory): IO[Vector[File]] = IO {
    val filesDirectory = new File(directory)

    if (filesDirectory.exists && filesDirectory.isDirectory) {
      filesDirectory.listFiles.filter(_.isFile).toVector
    } else {
      Vector.empty[File]
    }
  }

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


  def findOccurrencesInParticularFile(phrase: String, source: String, fileAbsPath: String): Vector[SubtitleOccurrenceDetails] = {
    val splitSourceBySeparateSubtitle = source.split("\r\n\r\n").toVector

    val parsedSubtitles = for {
      separateSubtitleInFile <- splitSourceBySeparateSubtitle
      subtitleParts          = separateSubtitleInFile.split("\r\n")
    } yield Subtitle(subtitleParts(0).toInt, subtitleParts(1), subtitleParts.drop(2).mkString(" "))

    val subtitlesThatContainPhrase = parsedSubtitles.filter(_.text.contains(phrase))

    subtitlesThatContainPhrase.map { subtitle =>
      val (startString, endString) = extractTimestampsFromSubtitleString(subtitle.timestamp)
      val startMillis = convertStringTimeToMillis(startString)
      val endMillis   = convertStringTimeToMillis(endString)

      SubtitleOccurrenceDetails(fileAbsPath, subtitle.number, startMillis, endMillis, subtitle.text)
    }
  }

  def findPhraseInFile(phrase: String, file: File): IO[Vector[SubtitleOccurrenceDetails]] = {
    val fileResource = makeReadFileResource(file)

    fileResource.use { fileBufferedSource =>
      IO(findOccurrencesInParticularFile(phrase, fileBufferedSource.mkString, file.getAbsolutePath))
    }
  }

}
