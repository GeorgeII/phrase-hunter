package com.github.georgeii.phrasehunter

import cats.effect.{ ExitCode, IO, IOApp }
import cats.implicits._
import com.github.georgeii.phrasehunter.config.Config
import com.github.georgeii.phrasehunter.resources.AppResources
import com.github.georgeii.phrasehunter.services.{ RecentHistory, Subtitles, VideoStreaming }
import dev.profunktor.redis4cats.log4cats._
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

object Main extends IOApp {

  def run(args: List[String]): IO[ExitCode] = {

    implicit val logger = Slf4jLogger.getLoggerFromName[IO]("Main logger")

    for {
      cfg <- Config.load[IO]
      _   <- Logger[IO].info(s"Loaded config $cfg")
      res <- AppResources.make[IO](cfg)

      _               <- Logger[IO].info("Initializing services...")
      subtitleService <- Subtitles.make[IO](res.postgres, res.redis, res.subtitlesDirectory).pure[IO]
      historyService  <- RecentHistory.make[IO](res.postgres, res.redis).pure[IO]
      videoService    <- VideoStreaming.make[IO](res.videoDirectory, cfg.videoDir.filesExtension, cfg.videoDir.bufferSize).pure[IO]
      _               <- Logger[IO].info("All services initialized.")

      exitCode <- PhraseHunterServer
        .stream[IO](
          cfg = cfg.httpServerConfig,
          resources = res,
          subtitlesService = subtitleService,
          recentHistoryService = historyService,
          videoService = videoService
        )
        .compile
        .drain
        .as(ExitCode.Success)
    } yield exitCode

  }

}
