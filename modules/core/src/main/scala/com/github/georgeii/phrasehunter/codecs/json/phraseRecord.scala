package com.github.georgeii.phrasehunter.codecs.json

import cats.syntax.either._
import io.circe.{ Decoder, Encoder }
import com.github.georgeii.phrasehunter.models.PhraseRecord
import com.github.georgeii.phrasehunter.util.RefinedCaster.toPhrase

import java.sql.Timestamp
import java.time.Instant

object phraseRecord {

  implicit val decodeTimestamp: Decoder[Timestamp] =
    Decoder.decodeLong.emap(long => Either.catchNonFatal(new Timestamp(long)).leftMap(_ => "timestamp"))

  implicit val encodeTimestamp: Encoder[Timestamp] = Encoder.encodeLong.contramap[Timestamp](_.getTime)

  implicit val encoder: Encoder[PhraseRecord] =
    Encoder.forProduct4("id", "phrase", "matchesNumber", "timestamp") { dbRecord =>
      (
        dbRecord.id,
        dbRecord.phrase.value.value,
        dbRecord.matchesNumber,
        dbRecord.timestamp
      )
    }

  implicit val decoder: Decoder[PhraseRecord] =
    Decoder.forProduct4("id", "phrase", "matchesNumber", "timestamp") { (id: Int, phrase: String, matches: Int, timestamp: Long) =>
      val phr = toPhrase(phrase, "Decoding from Json to PhraseRecord")

      PhraseRecord(id, phr, matches, Timestamp.from(Instant.ofEpochMilli(timestamp)))
    }

}
