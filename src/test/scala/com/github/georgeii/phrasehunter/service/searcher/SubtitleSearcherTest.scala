package com.github.georgeii.phrasehunter.service.searcher

//import cats.effect.IO
import munit.CatsEffectSuite

import java.io.File

class SubtitleSearcherTest extends CatsEffectSuite {

  val searcher = new SubtitleSearcher

  test("List all subtitle files available in the directory") {
    val filesIO = searcher.getVectorOfSubtitleFiles()
    assertIO(filesIO, Vector(new File("data/subtitles/.keep")))
  }

  test("") {

  }

}
