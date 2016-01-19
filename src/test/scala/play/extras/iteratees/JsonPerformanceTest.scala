package play.extras.iteratees

import java.io.File
import play.api.libs.iteratee._
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

object JsonPerformanceTest extends App {

  def jsonStream(fn: String) = {
    Enumerator.fromFile(new File(s"./src/test/resources/$fn")) &>
      Encoding.decode() &>
      JsonEnumeratees.jsArray
  }

  def test(fileName: String = "events.json") = {
    Await.result(
      jsonStream(fileName) |>>> Iteratee.ignore,
      Duration.Inf)
  }

  def time(block : => Unit): Long = {
    val start = System.currentTimeMillis()
    block
    System.currentTimeMillis() - start
  }

  println("<----- Degraded Performance Test ---->")
  println("Warmup: " + time((1 to 10).foreach(_ => test())) + "ms")
  println("1x:   " + time(test("events.json")) + "ms")
  println("10x:  " + time(test("events10x.json")) + "ms")
  println("100x: " + time(test("events100x.json")) + "ms")
  println("200x: " + time(test("events200x.json")) + "ms")
  println("400x: " + time(test("events400x.json")) + "ms")
  println("800x: " + time(test("events800x.json")) + "ms")

}
