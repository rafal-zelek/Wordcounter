package zelek.rafal.tech.task

import cats.Monoid
import enumeratum._

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration

sealed abstract class EventType(override val entryName: String) extends EnumEntry

case object EventType extends Enum[EventType] with CirceEnum[EventType] {
  object EventOne extends EventType("event_one")

  object EventTwo extends EventType("event_two")

  val values: IndexedSeq[EventType] = findValues
}

final case class NumberOfWords(numberOfWords: Int) extends AnyVal

object NumberOfWords {
  implicit val monoid: Monoid[NumberOfWords] = new Monoid[NumberOfWords] {
    override def empty: NumberOfWords = NumberOfWords(0)

    override def combine(x: NumberOfWords, y: NumberOfWords): NumberOfWords = NumberOfWords(x.numberOfWords + y.numberOfWords)
  }
}

final case class BlackBoxData(data: String) extends AnyVal

final case class BlackBoxEvent(eventType: EventType, data: BlackBoxData, timestamp: FiniteDuration) {
  private val WORD_SEPARATOR = " "

  def numberOfWords: NumberOfWords = NumberOfWords(data.data.split(WORD_SEPARATOR).length)
}


object BlackBoxEvent {

  import io.circe._
  import io.circe.generic.semiauto._

  implicit val FiniteDurationDecoder: Decoder[FiniteDuration] = Decoder.decodeLong.map(FiniteDuration(_, TimeUnit.MILLISECONDS))
  implicit val BlackBoxDataDecoder: Decoder[BlackBoxData] = Decoder.decodeString.map(BlackBoxData)
  implicit val blackBoxEventDecoder: Decoder[BlackBoxEvent] = deriveDecoder[BlackBoxEvent]
}