package com.github.georgeii.phrasehunter.models

import eu.timepit.refined.types.string.NonEmptyFiniteString
import io.estatico.newtype.macros.newtype

object phrase {

  type PhraseRefined = NonEmptyFiniteString[100]

  @newtype
  case class Phrase(value: PhraseRefined)
}
