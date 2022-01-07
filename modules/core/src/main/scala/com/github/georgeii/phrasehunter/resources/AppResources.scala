package com.github.georgeii.phrasehunter.resources

import cats.Applicative
import cats.effect.std.Console
import cats.effect.{ Async, Resource }
import cats.implicits._
import com.github.georgeii.phrasehunter.config.AppConfig
import com.github.georgeii.phrasehunter.config.AppConfig._
import dev.profunktor.redis4cats.{ Redis, RedisCommands }
import dev.profunktor.redis4cats.effect.MkRedis
import doobie.Transactor
import doobie.util.transactor.Transactor.Aux
import doobie.implicits._
import eu.timepit.refined.auto._
import fs2.io.net.Network
import org.typelevel.log4cats.Logger

sealed abstract class AppResources[F[_]](
    val postgres: Aux[F, Unit],
    val redis: Resource[F, RedisCommands[F, String, String]],
    val subtitlesDirectory: String,
    val videoDirectory: String
)

object AppResources {

  def make[F[_]: Async: Applicative: Console: Logger: MkRedis: Network](
      cfg: AppConfig
  ): F[AppResources[F]] = {

    def checkPostgresConnection(
        postgresXa: Aux[F, Unit]
    ): F[Unit] =
      sql"""select version();"""
        .query[String]
        .unique
        .transact(postgresXa)
        .flatMap { v =>
          Logger[F].info(s"Connected to Postgres $v")
        }

    def checkRedisConnection(
        redis: RedisCommands[F, String, String]
    ): F[Unit] =
      redis.info.flatMap {
        _.get("redis_version").traverse_ { v =>
          Logger[F].info(s"Connected to Redis $v")
        }
      }

    def mkPostgreSqlTransactor(c: PostgreSQLConfig): F[Aux[F, Unit]] = {
      val xa = Transactor.fromDriverManager[F](
        driver = c.driver.value,                                                        // driver classname
        url = s"jdbc:postgresql://${c.host.value}:${c.port.value}/${c.database.value}", // connect URL (driver-specific)
        user = c.user.value,                                                            // user
        pass = c.password.value                                                         // password
      )

      for {
        _ <- checkPostgresConnection(xa)
      } yield xa
    }

    def mkRedisResource(c: RedisConfig): Resource[F, RedisCommands[F, String, String]] =
      Redis[F].utf8(c.uri.value).evalTap(checkRedisConnection)

    /*
     * @return ./data/subtitles for IDE run; /path/setAsAnEnvVar/inBuild.sbt/data/subtitles when run in Docker
     */
    def getSubtitleDirectoryPath(c: SubtitleDirConfig): F[String] =
      for {
        dirPath <- (sys.env.getOrElse(c.envVariableName, ".") + c.subdirectoryLayout).pure[F]
        _       <- Logger[F].info(s"The directory with subtitle files: $dirPath")
      } yield dirPath

    /*
     * @return ./data/videos for IDE run; /path/setAsAnEnvVar/inBuild.sbt/data/videos when run in Docker
     */
    def getVideoDirectoryPath(c: VideoConfig): F[String] =
      for {
        dirPath <- (sys.env.getOrElse(c.envVariableName, ".") + c.subdirectoryLayout).pure[F]
        _       <- Logger[F].info(s"The directory with video files: $dirPath")
      } yield dirPath

    for {
      psqlXa    <- mkPostgreSqlTransactor(cfg.postgreSQL)
      redis     <- mkRedisResource(cfg.redis).pure[F]
      dirPath   <- getSubtitleDirectoryPath(cfg.subtitleDir)
      videoPath <- getVideoDirectoryPath(cfg.videoDir)
    } yield new AppResources[F](psqlXa, redis, dirPath, videoPath) {}
  }

}
