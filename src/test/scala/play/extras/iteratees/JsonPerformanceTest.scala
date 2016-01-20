package play.extras.iteratees

import java.io.File
import play.api.libs.iteratee._
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

object JsonPerformanceTest extends App {

  def vanillaStream(fn: String) = {
    Enumerator.fromFile(new File(s"./src/test/resources/$fn"))
  }

  def decodedStream(fn: String) = vanillaStream(fn) &> Encoding.decode()

  def jsonStream(fn: String) = decodedStream(fn) &> JsonEnumeratees.jsArray

  def test(fileName: String = "events.json")(f: String => Enumerator[_]) = {
    Await.result(
      f(fileName) |>>> Iteratee.ignore,
      Duration.Inf)
  }

  def time(block : => Unit): Long = {
    val start = System.currentTimeMillis()
    block
    System.currentTimeMillis() - start
  }

  println("Warmup: " + time((1 to 10).foreach(_ => test()(decodedStream))) + "ms")

  val xFactor = Seq(10, 100, 200, 400, 800)

  println("<----- Vanilla Enum Performance Test ---->")
  xFactor foreach { x =>
    println(s"${x}x: " + time(test(s"events${x}x.json")(vanillaStream)) + "ms")
  }

  println("<----- Decoding Performance Test ---->")
  xFactor foreach { x =>
    println(s"${x}x: " + time(test(s"events${x}x.json")(decodedStream)) + "ms")
  }

  println("<----- Json Parsing Performance Test ---->")
  xFactor foreach { x =>
    println(s"${x}x: " + time(test(s"events${x}x.json")(jsonStream)) + "ms")
  }
}
