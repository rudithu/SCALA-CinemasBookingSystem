package org.rudi.code.cinema.booking.strategy

import scala.collection.mutable

trait SeatAssignmentStrategy {
  def suggestSeats(bookingId: String, seatLayout: Array[Array[Int]], requiredSeats: Int): mutable.ListBuffer[Array[Int]]
  def selectSeats(bookingId: String, seatLayout: Array[Array[Int]], requiredSeats: Int, initSeatIndex: Array[Int]): mutable.ListBuffer[Array[Int]]
}
