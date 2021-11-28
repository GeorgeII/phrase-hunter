package com.github.georgeii.phrasehunter.services

import cats.effect.IO
import cats.syntax.traverse._
import com.github.georgeii.phrasehunter.models.SubtitleOccurrenceDetails
import munit.CatsEffectSuite

import java.io.File
import com.github.georgeii.phrasehunter.models.phrase.Phrase
import com.github.georgeii.phrasehunter.models.phrase.PhraseRefined

class SubtitlesTest extends CatsEffectSuite {

  val subtitleDirectory = "data/subtitles/"

  test("List all subtitle files available in the directory") {
    val filesIO = Subtitles.getAllSubtitleFilesInDirectory[IO]("data/subtitles/")
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
      found           = phrases.map(p => subtitleService.findAll(Phrase(p)))
    } yield found

    val flattenSubsFound: IO[List[List[SubtitleOccurrenceDetails]]] = subtitlesFound.map(_.sequence).flatten

    assertIO(
      flattenSubsFound.map(phrasesList => phrasesList.map(_.length)),
      List(
        47,
        3,
        0
      )
    )
//    val subtitlesIO1 = Subtitles.getSubtitlesWithPhraseInAllFiles(phrases.head, "data/subtitles/")
//    val subtitlesIO2 = searcher.getSubtitlesWithPhraseInAllFiles(phrase2, "data/subtitles/")
//    val subtitlesIO3 = searcher.getSubtitlesWithPhraseInAllFiles(phrase3, "data/subtitles/")
//
//    val foundMatches1 = subtitlesIO1.map(_.length)
//    val foundMatches2 = subtitlesIO2.map(_.length)
//    val foundMatches3 = subtitlesIO3.map(_.length)
//
//    assertIO(foundMatches1, 47)
//    assertIO(foundMatches2, 3)
//    assertIO(foundMatches3, 0)
  }

}
