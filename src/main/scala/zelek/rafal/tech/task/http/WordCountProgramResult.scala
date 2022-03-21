package zelek.rafal.tech.task.http


import io.circe._
import org.http4s.EntityEncoder
import org.http4s.circe.jsonEncoderOf
import zelek.rafal.tech.task.blackbox.domain.{EventType, NumberOfWords}

import scala.concurrent.duration.FiniteDuration

final case class WordCountProgramResult(collectedAt: FiniteDuration, numberOfWordsGroupedByEventType: Map[EventType, NumberOfWords])

object WordCountProgramResult {

  import io.circe.generic.semiauto._

  implicit val fooKeyEncoder: KeyEncoder[EventType] = (eventType: EventType) => eventType.entryName

  implicit val numberOfWordsEncoder: Encoder[NumberOfWords] = Encoder.encodeInt.contramap(_.numberOfWords)

  implicit val finiteDurationEncoder: Encoder[FiniteDuration] = Encoder.encodeLong.contramap(_.toMillis)

  implicit val wordCountProgramResultEncoder: Encoder[WordCountProgramResult] = deriveEncoder[WordCountProgramResult]

  implicit def wordCountProgramResultEntityEncoder[F[_]]: EntityEncoder[F, WordCountProgramResult] =
    jsonEncoderOf[F, WordCountProgramResult]
}
