package play.extras.iteratees

import org.specs2.mutable._

import play.api.libs.iteratee._
import scala.concurrent.Await
import scala.concurrent.duration._

object CsvSpec extends Specification {

  "csv values iteratee" should {
    "parse a single value" in {
      parseLine("value") must_== Seq("value")
    }

    "parse 2 values" in {
      parseLine("value1, value2") must_== Seq("value1", "value2")
    }

    "parse quoted values" in {
      parseLine(""" "a" ," b ",c  """) must_== Seq("a", " b ", "c")
    }

    "allow escaping of quotes" in {
      parseLine(""" "here is an "" escaped quote" """) must_== Seq("here is an \" escaped quote")
    }
  }

  "csv values enumeratee" should {
    "work" in {
      parseLines(
        """a,b
          |c,d""".stripMargin) must_== Seq(Seq("a", "b"), Seq("c", "d"))
    }
  }

  def parseLine(str: String) =
    Await.result(Enumerator(str.toCharArray:_*) |>>> Csv.values(), Duration.Inf)

  def parseLines(str: String) =
    Await.result(Enumerator(str.toCharArray:_*) &> Csv.csvLines |>>> Iteratee.getChunks, Duration.Inf)
}
