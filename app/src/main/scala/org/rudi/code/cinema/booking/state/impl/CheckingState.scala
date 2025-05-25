package org.rudi.code.cinema.booking.state.impl

import org.rudi.code.cinema.booking.constant.DataKey
import org.rudi.code.cinema.booking.state.State
import org.rudi.code.cinema.booking.constant.AppConst
import org.rudi.code.cinema.booking.util.LayoutUtil
import org.rudi.code.cinema.booking.service.{BookingService, ShowService}

import scala.annotation.tailrec
import scala.io.StdIn.readLine
import scala.util.matching.Regex

class CheckingState(bookingService: BookingService, showService: ShowService, layoutUtil: LayoutUtil.type ) extends State {

  private val pattern: Regex = AppConst.BOOKING_ID_PATTERN.r

  override def handle(): Map[DataKey, Any] = {
    
    @tailrec
    def prompt: Map[DataKey, Any] = {
      println("\nEnter booking id, or enter blank to go back to main menu:")
      print(AppConst.INPUT_PROMPT)
      val input = readLine().trim
      if (input.equalsIgnoreCase("")) {
        Map.empty
      } else {
        input match {
          case pattern(n) =>
            bookingService.findBooking(input) match {
              case Some(booking) =>
                println(s"\nBooking id: $input")
                println("Selected seats:\n")
                val seatLayout = showService.getSeatsLayout(booking.show.id)
                val layout = layoutUtil.generateLayout(seatLayout, booking.bookedSeatIndexes)
                layout.foreach(println)
              case _ =>
                println(s"Unable to find booking with ID: $input.")
            }
          case _ =>
            println(s"Invalid Booking ID: $input.")
        }
        prompt
      }
    }

    prompt
  }

}
