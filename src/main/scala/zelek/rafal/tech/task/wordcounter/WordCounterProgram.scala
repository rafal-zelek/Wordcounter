package zelek.rafal.tech.task.wordcounter

import cats.Functor
import cats.implicits._
import fs2.timeseries.TimeStamped
import fs2.{Pipe, Stream}
import io.circe.parser.decode
import log.effect.LogWriter
import zelek.rafal.tech.task.blackbox.domain.{BlackBoxEvent, EventType, NumberOfWords}
import zelek.rafal.tech.task.blackbox.source.BlackBoxSource

import scala.concurrent.duration._

class WordCounterProgram[F[_] : Functor](blackBoxSource: BlackBoxSource[F],
                                         log: LogWriter[F],
                                         wordCounterRepository: WordCounterRepository[F]) {
  private val TIMED_AGGREGATOR_RATE: FiniteDuration = 20.seconds

  private val parseBlackBoxEvent: Pipe[F, String, BlackBoxEvent] = _.map(decode[BlackBoxEvent])
    .flatMap {
      case Right(blackBoxEvent) => Stream(blackBoxEvent)
      case Left(error) =>
        Stream.eval(log.warn(s"Error ${error} during parsing data.")) *>
          Stream.empty
    }.evalTap(blackBoxEvent => log.debug(s"Successfully parsed ${blackBoxEvent}"))

  private val numberOfWordsTimedAggregator = TimeStamped.withRate[BlackBoxEvent, Map[EventType, NumberOfWords]](TIMED_AGGREGATOR_RATE)(
    event => Map(event.eventType -> event.numberOfWords)
  )

  private val collectAggregatedWords: Pipe[F, TimeStamped[Either[Map[EventType, NumberOfWords], BlackBoxEvent]], TimeStamped[Map[EventType, NumberOfWords]]] = _.flatMap {
    case TimeStamped(_, Right(_)) => Stream.empty
    case TimeStamped(timestamp, Left(value)) => Stream(TimeStamped(timestamp, value))
  }

  val program: Stream[F, TimeStamped[Map[EventType, NumberOfWords]]] = blackBoxSource.source
    .through(parseBlackBoxEvent)
    .map(event => TimeStamped(event.timestamp, event))
    .through(numberOfWordsTimedAggregator.toPipe)
    .through(collectAggregatedWords)
    .evalTap(wordCounterRepository.save)
    .evalTap(result => log.info(s"Result: $result"))

}