package com.github.georgeii.phrasehunter.codecs.redis

import com.github.georgeii.phrasehunter.codecs.redis.phrase.decodePhrase
import com.github.georgeii.phrasehunter.models.PhraseRecord
import com.github.georgeii.phrasehunter.models.phrase.Phrase
import dev.profunktor.redis4cats.codecs.splits._
import dev.profunktor.redis4cats.data.RedisCodec

import java.sql.Timestamp
import java.time.Instant
import scala.util.Try

object phraseRecord {

  val stringTimestampEpi: SplitEpi[String, Timestamp] =
    SplitEpi(
      s => Try(Timestamp.valueOf(s)).getOrElse(Timestamp.from(Instant.now())),
      _.toString
    )

  def encodePhraseRecord(phraseRecord: Map[String, String]): PhraseRecord = {
    val id            = phraseRecord.get("id").flatMap(str => Try(str.toInt).toOption).getOrElse(-1)
    val phrase        = phraseRecord.get("phrase").map(str => decodePhrase(str)).getOrElse(decodePhrase("No such key in Redis"))
    val matchesNumber = phraseRecord.get("matchesNumber").flatMap(str => Try(str.toInt).toOption).getOrElse(-1)
    val timestamp =
      phraseRecord.get("timestamp").flatMap(str => Try(Timestamp.valueOf(str)).toOption).getOrElse(Timestamp.from(Instant.now()))

    PhraseRecord(id, phrase, matchesNumber, timestamp)
  }

}
