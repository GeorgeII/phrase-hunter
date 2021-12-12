package com.github.georgeii.phrasehunter.models

/**
 * The class to store not extracted raw parts of subtitle. How a typical subtitle looks like:
 *
 * 171
 * 00:14:23,026 --> 00:14:26,827
 * More experienced ear
 * heard doorbell.
 *
 *
 * [A number of the subtitle].
 * [Timestamps when a phrase begins and ends].
 * [A phrase itself].
 */
case class Subtitle(number: Int, timestamp: String, text: String)
