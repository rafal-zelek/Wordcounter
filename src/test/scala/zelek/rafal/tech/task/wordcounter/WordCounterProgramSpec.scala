package zelek.rafal.tech.task.wordcounter

import cats.effect.IO
import fs2.Stream
import fs2.timeseries.TimeStamped
import log.effect.fs2.SyncLogWriter
import munit.CatsEffectSuite
import zelek.rafal.tech.task.blackbox.domain.{EventType, NumberOfWords}
import zelek.rafal.tech.task.blackbox.source.BlackBoxSource
import zelek.rafal.tech.task.TimedAggregatorRate

import scala.concurrent.duration._

class WordCounterProgramSpec extends CatsEffectSuite {

  test("aggregates two different event types after timed aggregator rate time passes") {
    val timedAggregatorRate = TimedAggregatorRate(5.seconds)
    val blackBoxSource = new BlackBoxSource[IO] {
      override val source: Stream[IO, String] = Stream(
        """{"eventType": "event_two", "data": "one two trzy cztery funf", "timestamp": "0"}""",
        """{"eventType": "event_one", "data": "dzień dobry", "timestamp": "5500"}""",
        """{"eventType": "event_two", "data": "jaś i małgosia", "timestamp": "5700"}"""
      )
    }
    val repositoryState = for {
      wordCounterRepository <- InMemoryWordCounterRepository()
      _ <- new WordCounterProgram(
        blackBoxSource,
        SyncLogWriter.consoleLog[IO],
        wordCounterRepository,
        timedAggregatorRate
      ).program.compile.drain
      numberOfWords <- wordCounterRepository.currentNumberOfWords
    } yield numberOfWords

    val expectedResult: TimeStamped[Map[EventType, NumberOfWords]] = TimeStamped(10.seconds, Map(
      EventType.EventOne -> NumberOfWords(2),
      EventType.EventTwo -> NumberOfWords(3)
    ))
    assertIO(repositoryState, expectedResult)
  }

  test("gracefully handles broken event stream") {
    val timedAggregatorRate = TimedAggregatorRate(5.seconds)
    val blackBoxSource = new BlackBoxSource[IO] {
      override val source: Stream[IO, String] = Stream(
        """{"eventType": "event_two", "data": "one two trzy cztery funf", "timestamp": "0"}""",
        """{"ehey im broken""",
        """{"eventType": "event_two", "data": "one two trzy cztery funf", "timestamp": "100"}""",
      )
    }
    val repositoryState = for {
      wordCounterRepository <- InMemoryWordCounterRepository()
      _ <- new WordCounterProgram(
        blackBoxSource,
        SyncLogWriter.consoleLog[IO],
        wordCounterRepository,
        timedAggregatorRate
      ).program.compile.drain
      numberOfWords <- wordCounterRepository.currentNumberOfWords
    } yield numberOfWords

    val expectedResult: TimeStamped[Map[EventType, NumberOfWords]] = TimeStamped(5.seconds, Map(
      EventType.EventTwo -> NumberOfWords(10)
    ))
    assertIO(repositoryState, expectedResult)
  }

  test("returns zero values on start time") {
    val timedAggregatorRate = TimedAggregatorRate(5.seconds)
    val blackBoxSource = new BlackBoxSource[IO] {
      override val source: Stream[IO, String] = Stream.empty
    }
    val repositoryState = for {
      wordCounterRepository <- InMemoryWordCounterRepository()
      _ <- new WordCounterProgram(
        blackBoxSource,
        SyncLogWriter.consoleLog[IO],
        wordCounterRepository,
        timedAggregatorRate
      ).program.compile.drain
      numberOfWords <- wordCounterRepository.currentNumberOfWords
    } yield numberOfWords

    val expectedResult: TimeStamped[Map[EventType, NumberOfWords]] = TimeStamped(0.seconds, Map.empty)
    assertIO(repositoryState, expectedResult)
  }
}
