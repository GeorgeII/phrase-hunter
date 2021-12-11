package com.github.georgeii.phrasehunter.codecs

import cats.syntax.either._
import com.github.georgeii.phrasehunter.models.PhraseRecord
import com.github.georgeii.phrasehunter.codecs.phrase._
import io.circe.{ Decoder, Encoder }
import io.circe.generic.semiauto.deriveEncoder

import java.sql.Timestamp

object phraseRecord {

  implicit val decodeTimestamp: Decoder[Timestamp] =
    Decoder.decodeLong.emap(long => Either.catchNonFatal(new Timestamp(long)).leftMap(_ => "timestamp"))

  implicit val encodeTimestamp: Encoder[Timestamp] = Encoder.encodeLong.contramap[Timestamp](_.getTime)

  implicit val encoder: Encoder[PhraseRecord] = deriveEncoder[PhraseRecord]
//    Encoder.forProduct4("id", "phrase", "matches-number", "timestamp") { dbRecord =>
//      (
//        dbRecord.id,
//        dbRecord.phrase,
//        dbRecord.matchesNumber,
//        dbRecord.timestamp
//      )
//    }

}
