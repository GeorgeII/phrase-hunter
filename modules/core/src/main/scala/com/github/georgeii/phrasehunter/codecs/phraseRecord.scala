package com.github.georgeii.phrasehunter.codecs

import cats.syntax.either._
import com.github.georgeii.phrasehunter.models.PhraseRecord
import io.circe.{ Decoder, Encoder }

import java.sql.Timestamp

object phraseRecord {

  implicit val decodeTimestamp: Decoder[Timestamp] =
    Decoder.decodeLong.emap(long => Either.catchNonFatal(new Timestamp(long)).leftMap(_ => "timestamp"))

  implicit val encodeTimestamp: Encoder[Timestamp] = Encoder.encodeLong.contramap[Timestamp](_.getTime)

  implicit val encoder: Encoder[PhraseRecord] =
    Encoder.forProduct4("id", "phrase", "matches-number", "timestamp") { dbRecord =>
      (
        dbRecord.id,
        dbRecord.phrase.value.value,
        dbRecord.matchesNumber,
        dbRecord.timestamp
      )
    }

}
