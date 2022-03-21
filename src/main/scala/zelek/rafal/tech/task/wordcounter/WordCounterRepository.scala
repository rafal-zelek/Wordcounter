package zelek.rafal.tech.task.wordcounter

import fs2.timeseries.TimeStamped
import zelek.rafal.tech.task.blackbox.domain.{EventType, NumberOfWords}

trait WordCounterRepository[F[_]] {
  def save(value: TimeStamped[Map[EventType, NumberOfWords]]): F[Unit]

  def currentNumberOfWords: F[TimeStamped[Map[EventType, NumberOfWords]]]
}
