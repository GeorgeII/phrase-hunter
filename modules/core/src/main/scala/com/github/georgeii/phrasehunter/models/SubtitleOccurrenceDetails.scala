package com.github.georgeii.phrasehunter.models

case class SubtitleOccurrenceDetails(
    fileAbsPath: String,
    phraseId: Int,
    startTimeMillis: Long,
    endTimeMillis: Long,
    text: String
)
