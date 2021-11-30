package com.github.georgeii.phrasehunter.models

case class SubtitleOccurrenceDetails(
    filename: String,
    phraseId: Int,
    startTimeMillis: Long,
    endTimeMillis: Long,
    text: String
)
