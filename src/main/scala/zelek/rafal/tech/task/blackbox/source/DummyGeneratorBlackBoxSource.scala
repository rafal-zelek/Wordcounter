package zelek.rafal.tech.task.blackbox.source

import cats.effect.IO
import fs2.{Pure, Stream}
import log.effect.LogWriter

import scala.concurrent.duration._


class DummyGeneratorBlackBoxSource(log: LogWriter[IO]) extends BlackBoxSource[IO] {
  private val TICK_DURATION: FiniteDuration = 50.milliseconds

  /**
    * event_kjkszpj is not parsable by circe by purpose to show different kind of errors
    */
  private val evenTypeStream: Stream[Pure, String] = Stream("event_one", "event_two", "event_kjkszpj").repeat
  private val dataStream: Stream[Pure, String] = Stream("The macOS blackbox", "generator", "doesn't work,", "but it does not", "stop me.").repeat
  private val timestamp: Stream[IO, FiniteDuration] = Stream.eval(IO.realTime)
  private val ticksStream: Stream[IO, FiniteDuration] = Stream.awakeEvery[IO](TICK_DURATION)

  override val source: Stream[IO, String] = {
    ticksStream zipRight (evenTypeStream zip dataStream) flatMap { case (eventType, data) =>
      timestamp.map(_.toMillis).map(timestamp =>
        createDataFeed(eventType, data, timestamp)
      ).evalTap(dataFeed => log.debug(s"Generated data feed: $dataFeed"))
    }
  }

  private def createDataFeed(eventType: String, data: String, timestamp: Long): String = {
    if (shouldValidDataFeedBeGenerated(timestamp))
      s"""{"eventType": "${eventType}", "data": "${data}", "timestamp": "${timestamp}"}"""
    else
      "broken tekst Łódź"
  }

  private def shouldValidDataFeedBeGenerated(timestamp: Long): Boolean = timestamp % 4 != 0
}
