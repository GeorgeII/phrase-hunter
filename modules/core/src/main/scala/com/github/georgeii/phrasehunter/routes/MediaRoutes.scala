package com.github.georgeii.phrasehunter.routes

import cats.Applicative
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.effect.Concurrent
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import org.http4s.circe.CirceEntityCodec._

import eu.timepit.refined.auto._

import com.github.georgeii.phrasehunter.models.media._
import com.github.georgeii.phrasehunter.codecs.media._
import com.github.georgeii.phrasehunter.services.Media

final case class MediaRoutes[F[_]: Concurrent](
    media: Media[F]
) extends Http4sDsl[F] {

  private[routes] val prefixPath = "/media"

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root =>
      Ok("I'm good!")

    case req @ POST -> Root =>
      for {
        _                  <- Applicative[F].pure(println("before"))
        name               <- req.as[MediaName]
        _                  <- Applicative[F].pure(println("after"))
        streamStartedMaybe <- media.sendMedia(name.title, "localhost", 8081).value
        _                  <- Applicative[F].pure(println("seems fine"))
        resp <- streamStartedMaybe match {
          case Right(_)    => Ok(s"Streaming of '$name' has started")
          case Left(value) => BadRequest(value)
        }
      } yield resp
  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )

}
