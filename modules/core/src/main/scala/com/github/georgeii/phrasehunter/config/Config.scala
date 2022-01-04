package com.github.georgeii.phrasehunter.config

import cats.effect.Async
import ciris._
import ciris.refined._
import com.comcast.ip4s._
import com.github.georgeii.phrasehunter.config.AppConfig._
import eu.timepit.refined.auto._
import eu.timepit.refined.cats._
import eu.timepit.refined.types.string.NonEmptyString

import scala.concurrent.duration._

object Config {

  def load[F[_]: Async]: F[AppConfig] =
    default[F].load

  private def default[F[_]]: ConfigValue[F, AppConfig] =
    env("SC_POSTGRES_PASSWORD").as[NonEmptyString].secret.map { postgresPassword =>
      AppConfig(
        PostgreSQLConfig(
          host = "postgres_container",
          port = 5432,
          user = "postgres",
          password = postgresPassword,
          database = "phrase-hunter",
          driver = "org.postgresql.Driver",
          max = 10
        ),
        RedisConfig(
          uri = RedisURI("redis://redis")
        ),
        HttpClientConfig(
          timeout = 60.seconds,
          idleTimeInPool = 30.seconds
        ),
        HttpServerConfig(
          host = host"0.0.0.0",
          port = port"8080"
        )
      )
    }

}
