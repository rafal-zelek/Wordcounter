package zelek.rafal.tech.task.blackbox.domain

import cats.Monoid

final case class NumberOfWords(numberOfWords: Int) extends AnyVal

object NumberOfWords {
  implicit val monoid: Monoid[NumberOfWords] = new Monoid[NumberOfWords] {
    override def empty: NumberOfWords = NumberOfWords(0)

    override def combine(x: NumberOfWords, y: NumberOfWords): NumberOfWords = NumberOfWords(x.numberOfWords + y.numberOfWords)
  }
}