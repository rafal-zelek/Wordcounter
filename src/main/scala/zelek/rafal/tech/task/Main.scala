package zelek.rafal.tech.task

import cats.effect.{ExitCode, IO, IOApp}
import log.effect.fs2.SyncLogWriter

object Main extends IOApp {
  def run(args: List[String]): IO[ExitCode] = {
    InMemoryWordCounterRepository().flatMap(wordCounterRepository => {
      val source = new DummyGeneratorBlackBoxSource(SyncLogWriter.consoleLog[IO])
      val program: WordCounterProgram[IO] = new WordCounterProgram(source, SyncLogWriter.consoleLog[IO], wordCounterRepository)
      val stream: fs2.Stream[IO, Nothing] = new WordCounterServer[IO](wordCounterRepository).stream
      program.program.merge(stream).compile.drain.as(ExitCode.Success)
    })
  }
}
