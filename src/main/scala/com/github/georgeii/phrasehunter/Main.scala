package com.github.georgeii.phrasehunter

import cats.effect.{ExitCode, IO, IOApp}

object Main extends IOApp {
  def run(args: List[String]) =
    PhrasehunterServer.stream[IO].compile.drain.as(ExitCode.Success)
}
