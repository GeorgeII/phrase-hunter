package com.github.georgeii.phrasehunter.programs.util

import cats.Applicative
import cats.effect.{ Resource, Sync }
import cats.implicits._

import uk.co.caprica.vlcj.factory.MediaPlayerFactory
import uk.co.caprica.vlcj.player.base.MediaPlayer

import java.io.File
import scala.io.{ BufferedSource, Source }

object FileReader {

  def getAllFilesInDirectory[F[_]: Applicative](directory: String): F[List[File]] = {
    val filesDirectory = new File(directory)

    if (filesDirectory.exists && filesDirectory.isDirectory) {
      filesDirectory.listFiles.filter(_.isFile).toList.pure[F]
    } else {
      List.empty[File].pure[F]
    }
  }

  def getNameNoExtension(file: File): String = {
    file.getName.split('.').toList.dropRight(1).mkString
  }

  def makeSubtitleFileResource[F[_]: Sync](file: File): Resource[F, BufferedSource] = {
    Resource.fromAutoCloseable {
      Applicative[F].pure(Source.fromFile(file))
    }
  }

  /**
    * @param mrl Media Resource Locator. The simplest example is "/user/username/videos/filename.mkv"
    */
  def makeMediaFileResource[F[_]: Applicative](mrl: String): Resource[F, MediaPlayer] =
    Resource.make {
      Applicative[F].pure {
        // the content of variables mutates
        val mediaPlayerFactory = new MediaPlayerFactory
        val mediaPlayer        = mediaPlayerFactory.mediaPlayers.newMediaPlayer()

        mediaPlayer.media().prepare(mrl)

        mediaPlayer
      }
    } { mediaPlayer =>
      Applicative[F].pure(mediaPlayer.release())
    }

}
