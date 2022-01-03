package com.github.georgeii.phrasehunter.services

import cats.Applicative
import cats.effect.{ Resource, Sync }
import cats.implicits._
import doobie.implicits._
import doobie.util.transactor.Transactor.Aux
import dev.profunktor.redis4cats.RedisCommands
import io.circe.Json

import com.github.georgeii.phrasehunter.models.phrase._
import com.github.georgeii.phrasehunter.models.{ PhraseRecord, Subtitle, SubtitleOccurrenceDetails }
import com.github.georgeii.phrasehunter.util.FileReader
import com.github.georgeii.phrasehunter.util.TimeConverter
import com.github.georgeii.phrasehunter.codecs.json.{ phraseRecord => phraseRecordJson }

import java.io.File
import java.sql.Timestamp
import java.time.Instant
import scala.util.Try

trait Subtitles[F[_]] {
  def findAll(phrase: Phrase): F[List[SubtitleOccurrenceDetails]]
  def create(phrase: Phrase, matchesNumber: Int): F[Int]
}

object Subtitles {
  def make[F[_]: Sync](
      postgresXa: Aux[F, Unit],
      redis: Resource[F, RedisCommands[F, String, String]],
      subtitleStorage: F[List[File]]
  ): Subtitles[F] =
    new Subtitles[F] {

      override def findAll(phrase: Phrase): F[List[SubtitleOccurrenceDetails]] =
        subtitleStorage.map { filesList =>
          filesList
            .map { file =>
              val fileResource        = FileReader.makeSubtitleFileResource[F](file)
              val filenameNoExtension = FileReader.getNameNoExtension(file)

              fileResource.use { bufferedSource =>
                Applicative[F].pure(findOccurrencesInFile(phrase, bufferedSource.mkString, filenameNoExtension))
              }
            }
            .sequence
            .map(_.flatten)
        }.flatten

      override def create(phrase: Phrase, matchesNumber: Int): F[Int] =
        for {
          id     <- createInPostgres(phrase, matchesNumber, postgresXa)
          record <- Applicative[F].pure(PhraseRecord(id, phrase, matchesNumber, Timestamp.from(Instant.now())))
          _      <- createInRedis(record, redis)
        } yield id
    }

  def extractInfoFromSubtitle(subtitle: String): Either[Throwable, Subtitle] = {
    val metaInfo = subtitle.split("\r\n")

    Try {
      Subtitle(metaInfo(0).toInt, metaInfo(1), metaInfo.drop(2).mkString(" "))
    }.toEither
  }

  def findOccurrencesInFile(
      phrase: Phrase,
      text: String,
      filename: String
  ): List[SubtitleOccurrenceDetails] = {
    val separateSubtitles = text.split("\r\n\r\n")

    val subtitlesThatContainPhrase = separateSubtitles.view
      .map(sub => extractInfoFromSubtitle(sub))
      .filter(_.isRight)
      .map(_.toOption.get)
      .filter(_.text.toLowerCase.contains(phrase.toString.toLowerCase))
      .map { sub =>
        val (startMillis, endMillis) = TimeConverter.extractStartEndTimestamps(sub.timestamp)

        SubtitleOccurrenceDetails(filename, sub.number, startMillis, endMillis, sub.text)
      }
      .toList

    subtitlesThatContainPhrase
  }

  def createInPostgres[F[_]: Sync](
      phrase: Phrase,
      matchesNumber: Int,
      postgresXa: Aux[F, Unit]
  ): F[Int] =
    sql"""
        insert into search_history (phrase, matches_number) values (${phrase.value.value}, $matchesNumber)
    """.update
      .withUniqueGeneratedKeys[Int]("id")
      .transact(postgresXa)

  def createInRedis[F[_]: Sync](
      phraseRecord: PhraseRecord,
      redis: Resource[F, RedisCommands[F, String, String]]
  ): F[Unit] =
    redis.use { redisCommands =>
      val record: Json = phraseRecordJson.encoder(phraseRecord)

      redisCommands.lPush("recent_history", record.noSpaces) *>
        redisCommands.lTrim("recent_history", 0, 9)
    }

}
