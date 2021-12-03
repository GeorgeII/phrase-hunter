package com.github.georgeii.phrasehunter.services

import cats.{ Applicative, Functor }
import cats.data.EitherT
import cats.effect.MonadCancelThrow
import cats.implicits._

import uk.co.caprica.vlcj.player.base.MediaPlayer
import com.github.georgeii.phrasehunter.programs.util.FileReader

import java.io.File

trait Media[F[_]] {
  def sendMedia(name: String, address: String, port: Int): EitherT[F, String, Unit]
}

object Media {

  def make[F[_]: MonadCancelThrow](
      mediaStorage: F[List[File]]
  ): Media[F] = {
    new Media[F] {

      override def sendMedia(name: String, address: String, port: Int): EitherT[F, String, Unit] = {
        val options = formatHttpStream(address, port)
        println(options)
        val mediaFileMaybe: EitherT[F, String, File] = findFile(name, mediaStorage)

        for {
          file <- mediaFileMaybe

          fileResource = FileReader.makeMediaFileResource[F](file.toString)
          _ <- EitherT.right {
            println("so far so good")
            fileResource.use(useMediaToSend[F](options))
          }
        } yield ()
      }
    }
  }

  private def formatHttpStream(address: String, port: Int): String = {
    s":sout=#duplicate{dst=std{access=http,mux=ts,dst=$address:$port}}"
  }

  private def findFile[F[_]: Functor](nameToFind: String, mediaStorage: F[List[File]]): EitherT[F, String, File] =
    EitherT {
      mediaStorage.map { files =>
        files
          .find(file => FileReader.getNameNoExtension(file) == nameToFind)
          .toRight(s"Media file '$nameToFind' not found")
      }
    }

  private def useMediaToSend[F[_]: Applicative](options: String)(mediaPlayer: MediaPlayer): F[Unit] =
    Applicative[F].pure {
      val mrl = mediaPlayer.media().info().mrl()

      mediaPlayer.media().play(mrl, options)
    }.void

}
