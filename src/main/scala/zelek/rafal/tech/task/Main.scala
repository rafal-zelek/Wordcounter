package zelek.rafal.tech.task

import cats.effect.{ExitCode, IO, IOApp}
import log.effect.fs2.SyncLogWriter
import zelek.rafal.tech.task.blackbox.source.DummyGeneratorBlackBoxSource
import zelek.rafal.tech.task.http.WordCounterServer
import zelek.rafal.tech.task.wordcounter.{InMemoryWordCounterRepository, WordCounterProgram}

import scala.concurrent.duration._

object Main extends IOApp {
  def run(args: List[String]): IO[ExitCode] = {
    val config = Config(TimedAggregatorRate(10.seconds), DummyGeneratorTickRate(20.milliseconds))
    InMemoryWordCounterRepository().flatMap(wordCounterRepository => {
      val blackBoxSource = new DummyGeneratorBlackBoxSource(
        SyncLogWriter.consoleLog[IO],
        config.dummyGeneratorTickRate
      )

      val wordCounterProgram = new WordCounterProgram(
        blackBoxSource,
        SyncLogWriter.consoleLog[IO],
        wordCounterRepository,
        config.timedAggregatorRate
      ).program

      val wordCounterServer = new WordCounterServer[IO](wordCounterRepository).stream

      (wordCounterProgram merge wordCounterServer).compile.drain.as(ExitCode.Success)
    })
  }
}
