package com.github.georgeii.phrasehunter.services

import cats.Applicative
import cats.effect.Sync
import cats.implicits._

import com.github.georgeii.phrasehunter.models.phrase._
import com.github.georgeii.phrasehunter.models.SubtitleOccurrenceDetails
import com.github.georgeii.phrasehunter.programs.util.FileReader
import com.github.georgeii.phrasehunter.programs.PhraseSearcher

import java.io.File

trait Subtitles[F[_]] {
  def findAll(phrase: Phrase): F[List[SubtitleOccurrenceDetails]]
}

object Subtitles {
  def make[F[_]: Sync](
      subtitleStorage: F[List[File]]
  ): Subtitles[F] = {
    new Subtitles[F] {
      override def findAll(phrase: Phrase): F[List[SubtitleOccurrenceDetails]] = {
        subtitleStorage.map { filesList =>
          filesList
            .map { file =>
              val fileResource        = FileReader.makeFileResource[F](file)
              val filenameNoExtension = file.getName.split('.').toList.dropRight(1).mkString

              fileResource.use { bufferedSource =>
                Applicative[F].pure(
                  PhraseSearcher.findOccurrencesInFile(phrase, bufferedSource.mkString, filenameNoExtension)
                )
              }
            }
            .sequence
            .map(_.flatten)
        }.flatten
      }
    }
  }

}
