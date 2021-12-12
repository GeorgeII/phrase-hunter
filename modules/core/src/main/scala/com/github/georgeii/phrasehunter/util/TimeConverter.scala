package com.github.georgeii.phrasehunter.util

import scala.util.chaining._

object TimeConverter {

  /**
   * @param timeString ex.: 00:02:19,482 --> 00:02:21,609   This is how it's written in .srt files.
   * @return ("00:02:19,482", "00:02:21,609")
   */
  private def extractTimestampsFromSubtitleString(timeString: String): (String, String) = {
    val startAndEndTimestamps = timeString.split(" --> ")

    (startAndEndTimestamps(0), startAndEndTimestamps(1))
  }

  /**
   * @param timestamp .srt specific timestamp in format like 00:02:19,482
   */
  private def convertStringTimeToMillis(timestamp: String): Long = {
    val hoursMinutesSecondsSplit = timestamp.split(":")
    val secondsMillisSplit       = hoursMinutesSecondsSplit(2).split(",")

    val hours   = hoursMinutesSecondsSplit(0).toLong
    val minutes = hoursMinutesSecondsSplit(1).toLong
    val seconds = secondsMillisSplit(0).toLong
    val millis  = secondsMillisSplit(1).toLong

    hours * 60 * 60 * 1000 + minutes * 60 * 1000 + seconds * 1000 + millis
  }

  // TODO: Refined type for a timeString
  def extractStartEndTimestamps(timeString: String): (Long, Long) =
    extractTimestampsFromSubtitleString(timeString).pipe {
      case (start, end) => (convertStringTimeToMillis(start), convertStringTimeToMillis(end))
    }

  def convertMillisToTime(timestamp: Long): String = {
    val millis = timestamp                      % 1000
    val second = (timestamp / 1000)             % 60
    val minute = (timestamp / (1000 * 60))      % 60
    val hour   = (timestamp / (1000 * 60 * 60)) % 24

    String.format("%02d:%02d:%02d.%d", hour, minute, second, millis)
  }

}
