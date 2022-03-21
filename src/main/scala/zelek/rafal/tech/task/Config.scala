package zelek.rafal.tech.task

import scala.concurrent.duration.FiniteDuration

final case class TimedAggregatorRate(rate: FiniteDuration) extends AnyVal

final case class DummyGeneratorTickRate(rate: FiniteDuration) extends AnyVal

case class Config(timedAggregatorRate: TimedAggregatorRate, dummyGeneratorTickRate: DummyGeneratorTickRate)
