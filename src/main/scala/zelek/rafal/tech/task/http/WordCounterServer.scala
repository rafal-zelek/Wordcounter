package zelek.rafal.tech.task.http

import cats.effect.{Async, Resource}
import cats.syntax.all._
import com.comcast.ip4s._
import fs2.Stream
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits._
import org.http4s.server.middleware.Logger
import zelek.rafal.tech.task.wordcounter.WordCounterRepository

class WordCounterServer[F[_] : Async](wordCounterRepository: WordCounterRepository[F]) {

  def stream: Stream[F, Nothing] = {
    for {
      _ <- Stream.resource(EmberClientBuilder.default[F].build)
      httpApp = WordCounterRoutes.wordCounterRoutes[F](wordCounterRepository).orNotFound

      finalHttpApp = Logger.httpApp(logHeaders = true, logBody = true)(httpApp)

      exitCode <- Stream.resource(
        EmberServerBuilder.default[F]
          .withHost(ipv4"0.0.0.0")
          .withPort(port"8080")
          .withHttpApp(finalHttpApp)
          .build >>
          Resource.eval(Async[F].never)
      )
    } yield exitCode
  }.drain
}
