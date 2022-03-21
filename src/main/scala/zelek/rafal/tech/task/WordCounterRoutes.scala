package zelek.rafal.tech.task

import cats.effect.Sync
import cats.implicits._
import fs2.timeseries.TimeStamped
import io.circe._
import org.http4s.circe.jsonEncoderOf
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityEncoder, HttpRoutes}

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration

final case class WordCountProgramResult(timestamp: FiniteDuration, result: Map[EventType, NumberOfWords])

object WordCountProgramResult {
  private val emptyValue = WordCountProgramResult(FiniteDuration.apply(0, TimeUnit.MILLISECONDS), Map.empty)

  def apply(optionalProgramResult: Option[TimeStamped[Map[EventType, NumberOfWords]]]): WordCountProgramResult = {
    optionalProgramResult.map(programResult => WordCountProgramResult(programResult.time, programResult.value))
      .getOrElse(emptyValue)
  }

  import io.circe.generic.semiauto._

  implicit val fooKeyEncoder: KeyEncoder[EventType] = (eventType: EventType) => eventType.entryName

  implicit val numberOfWordsEncoder: Encoder[NumberOfWords] = Encoder.encodeInt.contramap(_.numberOfWords)

  implicit val finiteDurationEncoder: Encoder[FiniteDuration] = Encoder.encodeLong.contramap(_.toMillis)

  implicit val wordCountProgramResultEncoder: Encoder[WordCountProgramResult] = deriveEncoder[WordCountProgramResult]

  implicit def wordCountProgramResultEntityEncoder[F[_]]: EntityEncoder[F, WordCountProgramResult] =
    jsonEncoderOf[F, WordCountProgramResult]
}

object WordCounterRoutes {

  def wordCounterRoutes[F[_] : Sync](program: fs2.Stream[F, TimeStamped[Map[EventType, NumberOfWords]]]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "words" / "now" =>
        for {
          result <- program.take(1).compile.last
          response <- Ok(WordCountProgramResult(result))
        } yield response
    }
  }
}