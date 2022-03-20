package zelek.rafal.tech.task

import cats.effect.{ExitCode, IO, IOApp}
import log.effect.fs2.SyncLogWriter

object Main extends IOApp {
  def run(args: List[String]): IO[ExitCode] = {
    new DummyGeneratorBlackBoxSource()(SyncLogWriter.consoleLog[IO]).source.compile.drain.as(ExitCode.Success)
  }
}
