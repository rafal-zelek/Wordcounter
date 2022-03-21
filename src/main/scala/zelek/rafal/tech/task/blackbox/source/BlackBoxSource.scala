package zelek.rafal.tech.task.blackbox.source

import fs2.Stream

trait BlackBoxSource[F[_]] {
  val source: Stream[F, String]
}
