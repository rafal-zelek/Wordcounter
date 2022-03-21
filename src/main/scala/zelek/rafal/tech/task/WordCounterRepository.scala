package zelek.rafal.tech.task

import cats.effect.IO
import cats.effect.kernel.Ref
import fs2.timeseries.TimeStamped

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration

trait WordCounterRepository[F[_]] {
  def save(value: TimeStamped[Map[EventType, NumberOfWords]]): F[Unit]

  def currentNumberOfWords: F[TimeStamped[Map[EventType, NumberOfWords]]]
}

class InMemoryWordCounterRepository(ref: Ref[IO, TimeStamped[Map[EventType, NumberOfWords]]]) extends WordCounterRepository[IO] {
  override def save(value: TimeStamped[Map[EventType, NumberOfWords]]): IO[Unit] = ref.set(value)

  override def currentNumberOfWords: IO[TimeStamped[Map[EventType, NumberOfWords]]] = ref.get
}

object InMemoryWordCounterRepository {
  def apply(): IO[InMemoryWordCounterRepository] = {
    val initialValue: TimeStamped[Map[EventType, NumberOfWords]] = TimeStamped(FiniteDuration(0, TimeUnit.MILLISECONDS), Map.empty)
    val ioRef = Ref.of[IO, TimeStamped[Map[EventType, NumberOfWords]]](initialValue)
    ioRef.map(new InMemoryWordCounterRepository(_))
  }
}