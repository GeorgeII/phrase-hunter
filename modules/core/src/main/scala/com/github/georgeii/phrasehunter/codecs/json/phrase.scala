package com.github.georgeii.phrasehunter.codecs.json

import io.circe.{ Decoder, Encoder }
import io.circe.refined._

import com.github.georgeii.phrasehunter.models.phrase._

object phrase {

  implicit val jsonDecoder: Decoder[Phrase] = Decoder.forProduct1("phrase")(Phrase.apply)

  implicit val jsonEncoder: Encoder[Phrase] = Encoder.forProduct1("phrase")(_.value)

}
