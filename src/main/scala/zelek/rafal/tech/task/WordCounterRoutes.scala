package zelek.rafal.tech.task

import cats.effect.Sync
import cats.implicits._
import io.circe._
import org.http4s.circe.jsonEncoderOf
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityEncoder, HttpRoutes}
import scala.concurrent.duration.FiniteDuration

final case class WordCountProgramResult(timestamp: FiniteDuration, result: Map[EventType, NumberOfWords])

object WordCountProgramResult {
  import io.circe.generic.semiauto._

  implicit val fooKeyEncoder: KeyEncoder[EventType] = (eventType: EventType) => eventType.entryName

  implicit val numberOfWordsEncoder: Encoder[NumberOfWords] = Encoder.encodeInt.contramap(_.numberOfWords)

  implicit val finiteDurationEncoder: Encoder[FiniteDuration] = Encoder.encodeLong.contramap(_.toMillis)

  implicit val wordCountProgramResultEncoder: Encoder[WordCountProgramResult] = deriveEncoder[WordCountProgramResult]

  implicit def wordCountProgramResultEntityEncoder[F[_]]: EntityEncoder[F, WordCountProgramResult] =
    jsonEncoderOf[F, WordCountProgramResult]
}

object WordCounterRoutes {
  def wordCounterRoutes[F[_] : Sync](wordCounterRepository: WordCounterRepository[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "words" / "now" =>
        for {
          currentNumberOfWords <- wordCounterRepository.currentNumberOfWords
//          result <- program.take(1).compile.last
          response <- Ok(WordCountProgramResult(currentNumberOfWords.time, currentNumberOfWords.value))
        } yield response
    }
  }
}