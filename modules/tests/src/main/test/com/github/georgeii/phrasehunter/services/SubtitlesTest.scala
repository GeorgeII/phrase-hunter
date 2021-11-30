package com.github.georgeii.phrasehunter.services

import cats.effect.IO
import cats.syntax.traverse._
import com.github.georgeii.phrasehunter.models.SubtitleOccurrenceDetails
import munit.CatsEffectSuite

import java.io.File
import com.github.georgeii.phrasehunter.models.phrase._
import eu.timepit.refined.api.RefType

class SubtitlesTest extends CatsEffectSuite {

  val subtitleDirectory = "data/subtitles/"

  test("List all subtitle files available in the directory") {
    val filesIO = Subtitles.getAllSubtitleFilesInDirectory[IO](subtitleDirectory)
    assertIO(
      filesIO,
      List(
        new File("data/subtitles/Murder by Death (1976).1080p.ita.eng.sub.ita.eng-MIRCrew.eng.srt"),
        new File("data/subtitles/No.Country.For.Old.Men.2007.1080p.BrRip.x264.YIFY.srt")
      )
    )
  }

  test("Number of files in the directory is correct") {
    val filesNumberIO = Subtitles.getAllSubtitleFilesInDirectory[IO](subtitleDirectory).map(_.length)
    assertIO(filesNumberIO, 2)
  }

  test("Get all the subtitles with a particular phrase") {
    val phrases = List(
      "well",
      "twelve",
      "This phrase is not in any subtitles"
    )

    val subtitlesFound: IO[List[IO[List[SubtitleOccurrenceDetails]]]] = for {
      filesList <- Subtitles.getAllSubtitleFilesInDirectory[IO](subtitleDirectory)
      subtitleService = Subtitles.make[IO](filesList)
      found = phrases.map(
        p =>
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
      )
    } yield found

    val flattenSubsFound: IO[List[List[SubtitleOccurrenceDetails]]] = subtitlesFound.map(_.sequence).flatten

    assertIO(
      flattenSubsFound.map(subtitlesForAPhrase => subtitlesForAPhrase.map(_.length)),
      List(
        46,
        3,
        0
      )
    )
  }

}
