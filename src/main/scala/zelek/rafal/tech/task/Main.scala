package zelek.rafal.tech.task

import cats.effect.kernel.Ref
import cats.effect.{ExitCode, IO, IOApp}
import fs2.timeseries.TimeStamped
import log.effect.fs2.SyncLogWriter

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration

object Main extends IOApp {
  def run(args: List[String]): IO[ExitCode] = {
    val initialValue: TimeStamped[Map[EventType, NumberOfWords]] = TimeStamped(FiniteDuration(0, TimeUnit.MILLISECONDS), Map.empty)
    val ioRef = Ref.of[IO, TimeStamped[Map[EventType, NumberOfWords]]](initialValue)
    ioRef.flatMap(ref => {
      val wordCounterRepository: WordCounterRepository[IO] = new InMemoryWordCounterRepository(ref)
      val source = new DummyGeneratorBlackBoxSource(SyncLogWriter.consoleLog[IO])
      val program: WordCounterProgram[IO] = new WordCounterProgram(source, SyncLogWriter.consoleLog[IO], wordCounterRepository)
      val stream: fs2.Stream[IO, Nothing] = new WordCounterServer[IO](wordCounterRepository).stream
      program.program.merge(stream).compile.drain.as(ExitCode.Success)
    })
  }
}
