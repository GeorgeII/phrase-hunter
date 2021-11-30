package com.github.georgeii.phrasehunter.programs

import cats.effect.Sync
import com.github.georgeii.phrasehunter.models.{ Subtitle, SubtitleOccurrenceDetails }
import com.github.georgeii.phrasehunter.models.phrase.Phrase
import com.github.georgeii.phrasehunter.programs.util.TimeConverter.extractStartEndTimestamps

object PhraseSearcher {

  private def extractInfoFromSubtitle(subtitle: String): Subtitle = {
    val metaInfo = subtitle.split("\r\n")

    Subtitle(metaInfo(0).toInt, metaInfo(1), metaInfo.drop(2).mkString(" ").toLowerCase)
  }

  def findOccurrencesInFile(
      phrase: Phrase,
      text: String,
      filename: String
  ): List[SubtitleOccurrenceDetails] = {
    val separateSubtitles = text.split("\r\n\r\n")

    val subtitlesThatContainPhrase = separateSubtitles.view
      .map(sub => extractInfoFromSubtitle(sub))
      .filter(_.text.contains(phrase.toString))
      .map { sub =>
        val (startMillis, endMillis) = extractStartEndTimestamps(sub.timestamp)

        SubtitleOccurrenceDetails(filename, sub.number, startMillis, endMillis, sub.text)
      }
      .toList

    subtitlesThatContainPhrase
  }

}
