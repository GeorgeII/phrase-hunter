package com.github.georgeii.phrasehunter.codecs

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

import com.github.georgeii.phrasehunter.models.media._

object media {

  implicit val decoder: Decoder[MediaName] = deriveDecoder[MediaName]

}
