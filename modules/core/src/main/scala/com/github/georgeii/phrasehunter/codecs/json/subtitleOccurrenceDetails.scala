package com.github.georgeii.phrasehunter.codecs.json

import io.circe.Encoder

import com.github.georgeii.phrasehunter.models.SubtitleOccurrenceDetails
import com.github.georgeii.phrasehunter.util.TimeConverter

object subtitleOccurrenceDetails {

  implicit val singleEncoder: Encoder[SubtitleOccurrenceDetails] =
    Encoder.forProduct4("name", "start-time-millis", "start-time-string", "text") { subtitleDetails =>
      (
        subtitleDetails.filename,
        subtitleDetails.startTimeMillis,
        TimeConverter.convertMillisToTime(subtitleDetails.startTimeMillis),
        subtitleDetails.text
      )
    }

}
