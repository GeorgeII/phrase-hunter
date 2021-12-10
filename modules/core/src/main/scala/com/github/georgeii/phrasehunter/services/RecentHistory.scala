package com.github.georgeii.phrasehunter.services

import cats.effect.{ Resource, Sync }
import doobie.implicits._
import doobie.util.transactor.Transactor.Aux

import com.github.georgeii.phrasehunter.models.phrase.Phrase

trait RecentHistory[F[_]] {
  def getAllRecent(n: Int): F[List[Phrase]]
  def getRecentFound(n: Int): F[List[Phrase]]
  def getRecentNotFound(n: Int): F[List[Phrase]]
}

object RecentHistory {
  def make[F[_]: Sync](
      postgresXa: Aux[F, Unit],
      redis: Resource[F, _]
  ): RecentHistory[F] =
    new RecentHistory[F] {
      override def getAllRecent(n: Int = 10): F[List[Phrase]] =
        sql"""
            select id, phrase, matches_number, timestamp from search_history
            order by id desc
        """
          .query[Phrase]
          .stream
          .take(n)
          .compile
          .toList
          .transact(postgresXa)

      override def getRecentFound(n: Int = 5): F[List[Phrase]] =
        sql"""
            select id, phrase, matches_number, timestamp from search_history
            where matches_number > 0
            order by id desc
        """
          .query[Phrase]
          .stream
          .take(n)
          .compile
          .toList
          .transact(postgresXa)

      override def getRecentNotFound(n: Int = 5): F[List[Phrase]] =
        sql"""
            select id, phrase, matches_number, timestamp from search_history
            where matches_number = 0
            order by id desc
        """
          .query[Phrase]
          .stream
          .take(n)
          .compile
          .toList
          .transact(postgresXa)
    }
}
