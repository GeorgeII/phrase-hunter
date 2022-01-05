package com.github.georgeii.phrasehunter.services

import cats.effect.{ Resource, Sync }
import cats.implicits._
import doobie.implicits._
import doobie.util.transactor.Transactor.Aux
import dev.profunktor.redis4cats.RedisCommands
import io.circe.parser._
import io.circe.Json
import com.github.georgeii.phrasehunter.models.PhraseRecord
import com.github.georgeii.phrasehunter.codecs.doobie.phraseRecord._
import com.github.georgeii.phrasehunter.codecs.json.{ phraseRecord => phraseRecordJson }
import org.typelevel.log4cats.Logger

trait RecentHistory[F[_]] {
  def getAllRecent(n: Int = 10): F[List[PhraseRecord]]
  def getRecentFound(n: Int = 5): F[List[PhraseRecord]]
  def getRecentNotFound(n: Int = 5): F[List[PhraseRecord]]
}

object RecentHistory {
  def make[F[_]: Sync: Logger](
      postgresXa: Aux[F, Unit],
      redis: Resource[F, RedisCommands[F, String, String]]
  ): RecentHistory[F] =
    new RecentHistory[F] {

      override def getAllRecent(n: Int = 10): F[List[PhraseRecord]] =
        if (n <= 10) getAllRecentFromRedis(n, redis)
        else getAllRecentFromPostgres(n, postgresXa)

      override def getRecentFound(n: Int = 5): F[List[PhraseRecord]] =
        sql"""
            select id, phrase, matches_number, timestamp from search_history
            where matches_number > 0
            order by id desc
        """
          .query[PhraseRecord]
          .stream
          .take(n.toLong)
          .compile
          .toList
          .transact(postgresXa)

      override def getRecentNotFound(n: Int = 5): F[List[PhraseRecord]] =
        sql"""
            select id, phrase, matches_number, timestamp from search_history
            where matches_number = 0
            order by id desc
        """
          .query[PhraseRecord]
          .stream
          .take(n.toLong)
          .compile
          .toList
          .transact(postgresXa)
    }

  private def getAllRecentFromRedis[F[_]: Sync: Logger](
      n: Int,
      redis: Resource[F, RedisCommands[F, String, String]]
  ): F[List[PhraseRecord]] =
    redis.use { redisCommands =>
      redisCommands
        .lRange("recent_history", 0L, n.toLong)
        .map(_.reverse)
        .map(list => list.map(str => phraseRecordJson.decoder.decodeJson(parse(str).getOrElse(Json.Null))))
        .map(_.sequence)
        .flatMap {
          case Right(list) => list.pure[F]
          case Left(decodingFailure) =>
            for {
              _ <- Logger[F].info(s"Failed to decode json from Redis. $decodingFailure")
            } yield List.empty[PhraseRecord]
        }
    }

  private def getAllRecentFromPostgres[F[_]: Sync](
      n: Int,
      postgresXa: Aux[F, Unit]
  ): F[List[PhraseRecord]] =
    sql"""
            select id, phrase, matches_number, timestamp from search_history
            order by id desc
        """
      .query[PhraseRecord]
      .stream
      .take(n.toLong)
      .compile
      .toList
      .transact(postgresXa)
}
