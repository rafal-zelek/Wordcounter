package zelek.rafal.tech.task.blackbox.domain

import enumeratum._

case object EventType extends Enum[EventType] with CirceEnum[EventType] {
  object EventOne extends EventType("event_one")

  object EventTwo extends EventType("event_two")

  val values: IndexedSeq[EventType] = findValues
}

sealed abstract class EventType(override val entryName: String) extends EnumEntry
