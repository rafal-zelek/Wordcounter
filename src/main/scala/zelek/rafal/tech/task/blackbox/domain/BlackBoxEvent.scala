package zelek.rafal.tech.task.blackbox.domain

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration



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