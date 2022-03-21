package zelek.rafal.tech.task.http

import cats.effect.Sync
import cats.implicits._
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import zelek.rafal.tech.task.wordcounter.WordCounterRepository

object WordCounterRoutes {
  def wordCounterRoutes[F[_] : Sync](wordCounterRepository: WordCounterRepository[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "words" / "now" =>
        for {
          currentNumberOfWords <- wordCounterRepository.currentNumberOfWords
          response <- Ok(WordCountProgramResult(currentNumberOfWords.time, currentNumberOfWords.value))
        } yield response
    }
  }
}