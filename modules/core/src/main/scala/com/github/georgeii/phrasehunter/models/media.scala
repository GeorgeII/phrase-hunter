package com.github.georgeii.phrasehunter.models

//import eu.timepit.refined.api.Refined
//import eu.timepit.refined.string.{ EndsWith, MatchesRegex }
//import eu.timepit.refined._
import io.estatico.newtype.macros.newtype

object media {

  // "Name Of The Movie (2004)". Checks for (xxxx) year pattern.
//  type MediaNameEndsYear = String Refined EndsWith[W.`"""\\([0-9]{4}\\)"""`.T]

  case class MediaName(title: String)

}
