package com.github.georgeii.phrasehunter.util

import cats.Applicative

object StaticResourceResolver {

  /**
   *
   * @return ./data/subtitles for IDE run; /path/setAsAnEnvVar/inBuild.sbt/data/subtitles when run in Docker
   */
  def getSubtitleDirectoryPath[F[_]: Applicative]: F[String] = Applicative[F].pure {
    val dataDir = sys.env.getOrElse("DATA_DIR", ".") + "/data/subtitles/"
    println(s"The directory with subtitle files: $dataDir")

    dataDir
  }

}
