package com.github.georgeii.phrasehunter.models

import eu.timepit.refined.api.Refined
import eu.timepit.refined.types.string.{ FiniteString, NonEmptyString }
import io.estatico.newtype.macros.newtype

object phrase {

  type PhraseRefined = NonEmptyString Refined FiniteString[100]

  @newtype
  case class Phrase(value: PhraseRefined)
}
