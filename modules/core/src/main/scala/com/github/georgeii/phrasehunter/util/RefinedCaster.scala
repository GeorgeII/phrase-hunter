package com.github.georgeii.phrasehunter.util

import com.github.georgeii.phrasehunter.models.phrase.{ Phrase, PhraseRefined }
import eu.timepit.refined.api.RefType
import eu.timepit.refined.auto._

object RefinedCaster {

  def toPhrase(phrase: String, whereCastedMessageToLog: String = ""): Phrase = {
    val maybeRefined = RefType.applyRef[PhraseRefined](phrase)

    maybeRefined match {
      case Right(refinedPhrase) => Phrase(refinedPhrase)
      case Left(value)          =>
        // TODO: replace with logging
        println(s"$whereCastedMessageToLog. Could not cast string to PhraseRefined type: $value")
        Phrase("Incorrect data. Phrase is empty or too long")
    }
  }

}
