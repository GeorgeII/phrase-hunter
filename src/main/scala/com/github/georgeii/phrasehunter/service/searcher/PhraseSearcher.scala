package com.github.georgeii.phrasehunter.service.searcher

import cats.effect.{IO, Resource}

import java.io.File
import java.util.Date
import scala.annotation.tailrec
import scala.io.{BufferedSource, Source}

case class SubtitleInfo(filename: File, phraseId: Int, startTime: Date, endTime: Date)
case class Occurrence(file: File, indices: Vector[Int])

case class PhraseTimestamp(start: Int, end: Int)
case class RawSubtitle(number: Int, timestamp: String, text: String)


class SubtitleSearcher {

  def getSubtitlesWithPhrase(phrase: String): Option[Vector[SubtitleInfo]] = {
    val allSubtitleFilesIO: IO[Vector[File]] = getAllSubtitleFiles()

    val filesWithOccurrences = for {
        subtitleFiles <- allSubtitleFilesIO
        file          <- subtitleFiles
        indices       <- getIndicesOf(file)
      } yield Occurrence(file, indices)

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

  def findOccurrencesInSource(pattern: String, source: BufferedSource): IO[Vector[PhraseTimestamp]] = {
    val chunkForEverySubtitle = source.mkString.split("\n\n").toList

    val parsedSubtitles = for {
      separateSubtitle <- chunkForEverySubtitle
      splitSubtitle     = separateSubtitle.split("\n")
    } yield RawSubtitle(splitSubtitle(0).toInt, splitSubtitle(1), splitSubtitle.drop(2).mkString)

    @tailrec
    def findMultipleOccurrences(from: Int, acc: Vector[Int]): Vector[Int] = {
      val idxOfOccurrence = sourceString.indexOfSlice(pattern, from)

      if (idxOfOccurrence == -1) acc
      else findMultipleOccurrences(idxOfOccurrence + pattern.length, acc :+ idxOfOccurrence)
    }

    val indicesWherePatternStarts: Vector[Int] = findMultipleOccurrences(0, Vector.empty[Int])


  }

  def findPhraseInFile(phrase: String, file: File): IO[Vector[PhraseTimestamp]] = {
    val fileResource = makeReadFileResource(file)

    fileResource.use { fileBufferedSource =>
      findOccurrencesInSource(phrase, fileBufferedSource)
    }
  }

}
