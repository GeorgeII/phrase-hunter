package com.github.georgeii.phrasehunter.codecs.doobie

import com.github.georgeii.phrasehunter.models.phrase._
import doobie.{ Get, Put }
import eu.timepit.refined.api.RefType
import eu.timepit.refined.auto._

object phrase {

  implicit val getPhrase: Get[Phrase] = Get[String].tmap { dbString =>
    val maybeRefined = RefType.applyRef[PhraseRefined](dbString)

    maybeRefined match {
      case Right(refinedPhrase) => Phrase(refinedPhrase)
      case Left(value)          =>
        // TODO: replace with logging
        println(s"Could not cast string from database to PhraseRefined type. $value")
        Phrase("Incorrect data in db. Phrase is empty or too long")
    }
  }

  implicit val putPhrase: Put[Phrase] = Put[String].tcontramap(_.value.value)

}
