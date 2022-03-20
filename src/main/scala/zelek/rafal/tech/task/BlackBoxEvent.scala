package zelek.rafal.tech.task

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

import scala.concurrent.duration.FiniteDuration

trait EventType
object EventOne extends EventType
object EventTwo extends EventType

final case class BlackBoxData(data: String) extends AnyVal

final case class BlackBoxEvent(eventType: EventType, data: BlackBoxData, timestamp: FiniteDuration)


object BlackBoxEvent {
  implicit val blackBoxEventDecoder: Decoder[BlackBoxEvent] = deriveDecoder[BlackBoxEvent]
}