package com.github.georgeii.phrasehunter.services

import cats.effect.IO
import cats.syntax.traverse._
import munit.CatsEffectSuite
import eu.timepit.refined.api.RefType
import eu.timepit.refined.auto._

import com.github.georgeii.phrasehunter.models.phrase._
import com.github.georgeii.phrasehunter.programs.util.FileReader
import com.github.georgeii.phrasehunter.models.SubtitleOccurrenceDetails

import java.io.File

class SubtitlesTest extends CatsEffectSuite {

  val subtitleFilesDirectory = "data/subtitles/"

  val filesList: IO[List[File]]      = FileReader.getAllFilesInDirectory[IO](subtitleFilesDirectory)
  val subtitleService: Subtitles[IO] = Subtitles.make[IO](filesList)

  test("List all subtitle files available in the directory") {
    assertIO(
      filesList,
      List(
        new File("data/subtitles/Murder by Death (1976).srt"),
        new File("data/subtitles/No Country For Old Men (2007).srt")
      )
    )
  }

  test("Number of files in the directory is correct") {
    val filesNumberIO = filesList.map(_.length)
    assertIO(filesNumberIO, 2)
  }

  test("Get all the subtitles with a particular phrase") {
    val phrases = List(
      "well",
      "twelve",
      "This phrase is not in any subtitles"
    )

    val subtitlesFound: List[IO[List[SubtitleOccurrenceDetails]]] = phrases
      .map { p =>
        subtitleService.findAll(
          Phrase(
            RefType.applyRef[PhraseRefined](p) match {
              case Left(value) =>
                throw new IllegalArgumentException(
                  s"A phrase should be non-empty and less than 100 characters long. $value"
                )
              case Right(value) => value
            }
          )
        )
      }

    val flattenSubsFound: IO[List[List[SubtitleOccurrenceDetails]]] = subtitlesFound.sequence

    assertIO(
      flattenSubsFound.map(subtitlesForAPhrase => subtitlesForAPhrase.map(_.length)),
      List(46, 3, 0)
    )
  }

  test("A concrete cherry-picked phrase in a concrete subtitle") {
    val results = subtitleService.findAll(Phrase("and we won't argue about it"))

    assertIO(
      results,
      List(
        SubtitleOccurrenceDetails(
          "No Country For Old Men (2007)",
          365,
          2_453_092,
          2_456_092,
          "Why don't I just set you down around here and we won't argue about it."
        )
      )
    )
  }

}
