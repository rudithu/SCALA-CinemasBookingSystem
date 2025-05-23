package org.rudi.code.cinema.booking.models

import java.util.UUID
import scala.collection.mutable

case class Movie(id: String, title: String)

object Movie {
  def apply(title: String): Movie = {
    val id = UUID.randomUUID().toString
    new Movie(id, title)
  }
}

case class Show(id: String, movie: Movie, auditoriumId: String)

object Show {
  def apply(movie: Movie, auditoriumId: String): Show = {
    val id = UUID.randomUUID().toString
    new Show(id, movie, auditoriumId)
  }
}

case class Auditorium(id: String, seats: Array[Array[Int]]) {
  def availableSeatCount: Int = seats.flatten.count(_ == 0)
}

object Auditorium {
  def apply(rows: Int, cols: Int): Auditorium = {
    val id = UUID.randomUUID().toString
    val seats = Array.ofDim[Int](rows, cols)
    new Auditorium(id, seats)
  }
}

case class Booking(id: String, var bookedSeatIndexes: mutable.ListBuffer[Array[Int]], show: Show)

object Booking {
  def apply(id: String, show: Show): Booking = {
    new Booking(id, mutable.ListBuffer.empty, show)
  }
}
