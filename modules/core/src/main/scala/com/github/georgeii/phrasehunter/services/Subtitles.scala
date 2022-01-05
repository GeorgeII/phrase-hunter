package com.github.georgeii.phrasehunter.services

import cats.{ Applicative, Parallel }
import cats.effect.{ Resource, Sync }
import cats.implicits._
import doobie.implicits._
import doobie.util.transactor.Transactor.Aux
import dev.profunktor.redis4cats.RedisCommands
import io.circe.Json
import com.github.georgeii.phrasehunter.models.phrase._
import com.github.georgeii.phrasehunter.models.{ PhraseRecord, SubtitleOccurrenceDetails }
import com.github.georgeii.phrasehunter.util.FileReader
import com.github.georgeii.phrasehunter.codecs.json.{ phraseRecord => phraseRecordJson }
import org.typelevel.log4cats.Logger

import java.sql.Timestamp
import java.time.Instant

trait Subtitles[F[_]] {
  def findAll(phrase: Phrase): F[List[SubtitleOccurrenceDetails]]
  def create(phrase: Phrase, matchesNumber: Int): F[Int]
}

object Subtitles {
  def make[F[_]: Sync: Parallel: Logger](
      postgresXa: Aux[F, Unit],
      redis: Resource[F, RedisCommands[F, String, String]],
      subtitlesDirectory: String
  ): Subtitles[F] =
    new Subtitles[F] {

      override def findAll(phrase: Phrase): F[List[SubtitleOccurrenceDetails]] =
        for {
          filesList <- FileReader.getAllFilesInDirectory[F](subtitlesDirectory)
          foundOccurrences <- filesList.parFlatTraverse { file =>
            val fileResource        = FileReader.makeSubtitleFileResource[F](file)
            val filenameNoExtension = FileReader.getNameNoExtension(file)

            fileResource.use { bufferedSource =>
              Applicative[F].pure(
                FileReader.findOccurrencesInFile(phrase, bufferedSource.mkString, filenameNoExtension)
              )
            }
          }
        } yield foundOccurrences

      override def create(phrase: Phrase, matchesNumber: Int): F[Int] =
        for {
          id     <- createInPostgres(phrase, matchesNumber, postgresXa)
          _      <- Logger[F].info(s"Phrase info stored in database by id $id")
          record <- Applicative[F].pure(PhraseRecord(id, phrase, matchesNumber, Timestamp.from(Instant.now())))
          _      <- createInRedis(record, redis)
          _      <- Logger[F].info("Phrase info cached into Redis")
        } yield id
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
