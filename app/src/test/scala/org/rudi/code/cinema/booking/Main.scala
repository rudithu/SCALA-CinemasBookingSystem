package org.rudi.code.cinema.booking

object Main {

  def main(arg: Array[String]): Unit = {
    println("rudi")

    var i: Int = 30
    (0 until 9).iterator.takeWhile(_ => i > 0).foreach(
      index => {
        println(s"index: $index, i: $i")
        i = i - 1
      }
    )

  }
}