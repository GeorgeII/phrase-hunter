package com.github.georgeii.phrasehunter.services

import cats.effect.{ Resource, Sync }
import doobie.implicits._
import doobie.util.transactor.Transactor.Aux

import com.github.georgeii.phrasehunter.models.PhraseRecord
import com.github.georgeii.phrasehunter.codecs.doobie.phraseRecord._

trait RecentHistory[F[_]] {
  def getAllRecent(n: Int = 10): F[List[PhraseRecord]]
  def getRecentFound(n: Int = 5): F[List[PhraseRecord]]
  def getRecentNotFound(n: Int = 5): F[List[PhraseRecord]]
}

object RecentHistory {
  def make[F[_]: Sync](
      postgresXa: Aux[F, Unit],
      redis: Resource[F, _]
  ): RecentHistory[F] =
    new RecentHistory[F] {
      override def getAllRecent(n: Int = 10): F[List[PhraseRecord]] =
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
}
