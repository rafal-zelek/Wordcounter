package zelek.rafal.tech.task

import cats.Functor
import cats.implicits._
import fs2.{Pipe, Stream}
import io.circe.parser.decode
import log.effect.LogWriter

class BlackBoxWordCountProgram[F[_] : Functor](blackBoxSource: BlackBoxSource[F],
                                               log: LogWriter[F]) {
  private val decodeBlackBoxEvent: String => Either[io.circe.Error, BlackBoxEvent] = decode[BlackBoxEvent]

  private val parseBlackBoxEvent: Pipe[F, String, BlackBoxEvent] = _.map(decodeBlackBoxEvent)
    .flatMap {
      case Right(blackBoxEvent) => Stream(blackBoxEvent)
      case Left(error) =>
        Stream.eval(log.warn(s"Error ${error} during parsing data.")) *>
          Stream.empty
    }.evalTap(blackBoxEvent => log.debug(s"Successfully parsed ${blackBoxEvent}"))

  val program: Stream[F, BlackBoxEvent] = blackBoxSource.source
    .through(parseBlackBoxEvent)
}
