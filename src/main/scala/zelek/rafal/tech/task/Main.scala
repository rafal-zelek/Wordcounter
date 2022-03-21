package zelek.rafal.tech.task

import cats.effect.{ExitCode, IO, IOApp}
import log.effect.fs2.SyncLogWriter

object Main extends IOApp {
  def run(args: List[String]): IO[ExitCode] = {
    val source = new DummyGeneratorBlackBoxSource(SyncLogWriter.consoleLog[IO])
    val program: BlackBoxWordCountProgram[IO] = new BlackBoxWordCountProgram(source, SyncLogWriter.consoleLog[IO])
    val stream: fs2.Stream[IO, Nothing] = new WordCounterServer[IO](program).stream
    stream.compile.drain.as(ExitCode.Success)
  }
}
