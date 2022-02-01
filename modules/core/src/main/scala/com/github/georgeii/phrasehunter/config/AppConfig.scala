package com.github.georgeii.phrasehunter.config

import ciris.Secret
import com.comcast.ip4s.{ Host, Port }
import eu.timepit.refined.types.net.UserPortNumber
import eu.timepit.refined.types.numeric.PosInt
import eu.timepit.refined.types.string.NonEmptyString
import io.estatico.newtype.macros.newtype

import scala.concurrent.duration.FiniteDuration

import AppConfig._

case class AppConfig(
    postgreSQL: PostgreSQLConfig,
    redis: RedisConfig,
    subtitleDir: SubtitleDirConfig,
    videoDir: VideoConfig,
    assetsDir: AssetConfig,
    httpClientConfig: HttpClientConfig,
    httpServerConfig: HttpServerConfig
)

object AppConfig {

  case class PostgreSQLConfig(
      host: NonEmptyString,
      port: UserPortNumber,
      user: NonEmptyString,
      password: Secret[NonEmptyString],
      database: NonEmptyString,
      driver: NonEmptyString,
      max: PosInt
  )

  @newtype case class RedisURI(value: NonEmptyString)
  @newtype case class RedisConfig(uri: RedisURI)

  case class SubtitleDirConfig(
      envVariableName: NonEmptyString,
      subdirectoryLayout: String
  )

  case class VideoConfig(
      envVariableName: NonEmptyString,
      subdirectoryLayout: String,
      filesExtension: String,
      bufferSize: Int
  )

  case class AssetConfig(
      envVariableName: NonEmptyString,
      subdirectoryLayout: String
  )

  case class HttpServerConfig(
      host: Host,
      port: Port
  )

  case class HttpClientConfig(
      timeout: FiniteDuration,
      idleTimeInPool: FiniteDuration
  )

}
