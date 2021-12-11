package com.github.georgeii.phrasehunter.models

import com.github.georgeii.phrasehunter.models.phrase.Phrase

import java.sql.Timestamp

case class PhraseRecord(id: Int, phrase: Phrase, matchesNumber: Int, timestamp: Timestamp)
