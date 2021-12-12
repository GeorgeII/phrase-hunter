package com.github.georgeii.phrasehunter

import cats.effect.{ ExitCode, IO, IOApp }

object Main extends IOApp {
  def run(args: List[String]): IO[ExitCode] =
//    implicit val logger = Slf4jLogger.getLogger[IO]
    PhraseHunterServer.stream[IO].compile.drain.as(ExitCode.Success)
}
