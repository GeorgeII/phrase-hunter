package com.github.georgeii.phrasehunter.codecs

import io.circe.refined._
import io.circe.{ Decoder, Encoder }

import com.github.georgeii.phrasehunter.models.phrase._

object phrase {

  implicit val jsonDecoder: Decoder[Phrase] = Decoder.forProduct1("phrase")(Phrase.apply)

  implicit val jsonEncoder: Encoder[Phrase] = Encoder.forProduct1("phrase")(_.value)

}
