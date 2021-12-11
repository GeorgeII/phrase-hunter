package com.github.georgeii.phrasehunter.codecs.doobie

import com.github.georgeii.phrasehunter.models.PhraseRecord
import com.github.georgeii.phrasehunter.models.phrase.Phrase
import com.github.georgeii.phrasehunter.codecs.doobie.phrase._
import doobie.{ Read, Write }
import doobie.implicits._
import doobie.implicits.javasql._

import java.sql.Timestamp

object phraseRecord {

  implicit val readPhraseRecord: Read[PhraseRecord] = Read[(Int, Phrase, Int, Timestamp)].map { dbTableTuple =>
    PhraseRecord(dbTableTuple._1, dbTableTuple._2, dbTableTuple._3, dbTableTuple._4)
  }

  implicit val writePhraseRecord: Write[PhraseRecord] = Write[(Phrase, Int)].contramap(p => (p.phrase, p.matchesNumber))

}
