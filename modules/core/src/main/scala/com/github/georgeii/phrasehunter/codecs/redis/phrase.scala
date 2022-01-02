package com.github.georgeii.phrasehunter.codecs.redis

import com.github.georgeii.phrasehunter.models.phrase._
import dev.profunktor.redis4cats.codecs.splits.SplitEpi
import eu.timepit.refined.api.RefType
import eu.timepit.refined.auto._

object phrase {

  val stringPhraseEpi: SplitEpi[String, Phrase] =
    SplitEpi(
      s => decodePhrase(s),
      _.value.value
    )

  def decodePhrase(str: String): Phrase = {
    val maybeRefined = RefType.applyRef[PhraseRefined](str)

    maybeRefined match {
      case Right(refinedPhrase) => Phrase(refinedPhrase)
      case Left(value)          =>
        // TODO: replace with logging
        println(s"Could not cast string from Redis to PhraseRefined type: $value")
        Phrase("Incorrect data in Redis. Phrase is empty or too long")
    }
  }

}
