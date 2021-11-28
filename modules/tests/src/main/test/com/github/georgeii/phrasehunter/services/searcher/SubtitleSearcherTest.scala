package com.github.georgeii.phrasehunter.services.searcher

//import cats.effect.IO
import munit.CatsEffectSuite

import java.io.File

class SubtitleSearcherTest extends CatsEffectSuite {

  val searcher = new SubtitleSearcher

  test("List all subtitle files available in the directory") {
    val filesIO = searcher.getVectorOfSubtitleFilesInDirectory("data/subtitles/")
    assertIO(
      filesIO,
      Vector(
        new File("data/subtitles/Murder by Death (1976).1080p.ita.eng.sub.ita.eng-MIRCrew.eng.srt"),
        new File("data/subtitles/No.Country.For.Old.Men.2007.1080p.BrRip.x264.YIFY.srt")
      )
    )
  }

  test("Number of files in the directory is correct") {
    val filesNumberIO = searcher.getVectorOfSubtitleFilesInDirectory("data/subtitles/").map(_.length)
    assertIO(filesNumberIO, 2)
  }

  test("Get all the subtitles with a particular phrase") {
    val phrase1 = "well"
    val phrase2 = "twelve"
    val phrase3 = "This phrase is not in any subtitles"

    val subtitlesIO1 = searcher.getSubtitlesWithPhraseInAllFiles(phrase1, "data/subtitles/")
    val subtitlesIO2 = searcher.getSubtitlesWithPhraseInAllFiles(phrase2, "data/subtitles/")
    val subtitlesIO3 = searcher.getSubtitlesWithPhraseInAllFiles(phrase3, "data/subtitles/")

    val foundMatches1 = subtitlesIO1.map(_.length)
    val foundMatches2 = subtitlesIO2.map(_.length)
    val foundMatches3 = subtitlesIO3.map(_.length)

    assertIO(foundMatches1, 47)
    assertIO(foundMatches2, 3)
    assertIO(foundMatches3, 0)
  }

}
