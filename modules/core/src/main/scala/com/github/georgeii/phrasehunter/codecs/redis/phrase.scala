package com.github.georgeii.phrasehunter.codecs.redis

import dev.profunktor.redis4cats.codecs.splits.SplitEpi

import com.github.georgeii.phrasehunter.models.phrase._
import com.github.georgeii.phrasehunter.util.RefinedCaster.toPhrase

object phrase {

  val stringPhraseEpi: SplitEpi[String, Phrase] =
    SplitEpi(
      s => decodePhrase(s),
      _.value.value
    )

  def decodePhrase(str: String): Phrase = toPhrase(str, "Decoding in Redis from String to Phrase")

}
