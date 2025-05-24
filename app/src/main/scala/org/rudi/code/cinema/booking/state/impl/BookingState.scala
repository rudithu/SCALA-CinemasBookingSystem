package org.rudi.code.cinema.booking.state.impl

import org.rudi.code.cinema.booking.constant.DataKey.SHOW_ID
import org.rudi.code.cinema.booking.constant.{AppConst, DataKey}
import org.rudi.code.cinema.booking.models.Booking
import org.rudi.code.cinema.booking.service.{BookingService, ShowService}
import org.rudi.code.cinema.booking.state.State
import org.rudi.code.cinema.booking.util.{LayoutUtil, SeatUtil}

import scala.annotation.tailrec
import scala.collection.mutable
import scala.io.StdIn.readLine

class BookingState(bookingService: BookingService, showService: ShowService, layoutUtil: LayoutUtil.type, seatUtil: SeatUtil.type, sessionData: mutable.Map[DataKey, Any]) extends State {

  private val REGEX_REQ_SEAT = "^(\\d+)$"
  private val CANCEL: Int = -1

  private val reqSeatPattern = REGEX_REQ_SEAT.r
  private val startSeatPattern = AppConst.SEAT_INDEX_PATTERN.r


  override def handle(): Map[DataKey, Any] = {
    getSeatAvailability match {
      case CANCEL => Map.empty
      case 0 =>
        println("Booking failed: No seats selected.\n")
        Map.empty
      case requiredSeat => reserveAndUpdateSeats(requiredSeat)
    }
  }

  private def reserveAndUpdateSeats(requiredSeats: Int): Map[DataKey, Any] = {
    getAsType[String](SHOW_ID, sessionData) match {
      case None => Map.empty
      case Some(showId) =>
        //default seat reservation
        var booking: Booking = bookingService.createTempBooking(showId, requiredSeats)
        println(s"\nSuccessfully reserved ${booking.bookedSeatIndexes.size} ${booking.show.movie.title} ticket${if (booking.bookedSeatIndexes.size > 1) "s" else ""}.")

        //change seat
        booking = updateBooking(booking)

        //confirm booking
        bookingService.confirmSeats(booking.id)
        println(s"\nBooking id: ${booking.id} confirmed.\n")

        Map(DataKey.BOOKING_ID -> booking.id)
    }
  }

  private def updateBooking(booking: Booking): Booking = {
    @tailrec
    def prompt(currentBooking: Booking): Booking = {
      val seatsLayout: Array[Array[Int]] = showService.getSeatsLayout(currentBooking.show.id)
      showSeatLayout(currentBooking, seatsLayout)

      println("\nEnter blank to accept seat selection, or enter new seating position")
      print(AppConst.INPUT_PROMPT)
      val input = readLine().trim

      if (input.equalsIgnoreCase("")) {
        currentBooking
      } else {
        prompt(
          if (validateSeatSelection(input, seatsLayout.length, seatsLayout(0).length)) {
            bookingService.changeSeats(booking.id, input)
          } else {
            currentBooking
          }
        )
      }
    }

    prompt(booking)
  }

  private def showSeatLayout(booking: Booking, seatsLayout: Array[Array[Int]]): Unit = {
    println(s"Booking id: ${booking.id}")
    println(s"Selected seat${if (booking.bookedSeatIndexes.size > 1) "s" else ""}:")
    println()
    val display = layoutUtil.generateLayout(seatsLayout, booking.bookedSeatIndexes)
    display.foreach(d => println(d))
  }

  private def validateSeatSelection(input: String, maxRow: Int, maxCol: Int): Boolean = {
    input match {
      case startSeatPattern(r, c) =>
        val seatIndex = seatUtil.getSeatIndexFromString(input)
        val seatRow = seatIndex(0)
        val seatCol = seatIndex(1)
        if (seatRow >= maxRow || seatCol >= maxCol || seatRow < 0 || seatCol < 0) {
          println(s"Invalid seat Number: $input.")
          false
        } else {
          true
        }
      case _ =>
        println(s"Invalid seat Number: $input.")
        false
    }
  }

  private def getSeatAvailability: Int = {
    @tailrec
    def prompt: Int = {
      println("Enter number of tickets to book, or enter blank to go back to main menu:")
      print(AppConst.INPUT_PROMPT)
      val input = readLine().trim
      if (input.equalsIgnoreCase("")) {
        CANCEL
      } else if (validateSeatAvailabilityInput(input)) {
        input.toInt
      } else {
        prompt
      }
    }

    prompt
  }

  private def validateSeatAvailabilityInput(input: String) = {
    input match {
      case reqSeatPattern(s) =>
        getAsType[String](SHOW_ID, sessionData) match {
          case None => false
          case Some(showId) =>
            val availableSeats = showService.getAvailableSeatCount(showId)
            if (availableSeats < s.toInt) {
              println(s"\nSorry, there ${if (availableSeats > 1) "are" else "is"} only $availableSeats seat${if (availableSeats > 1) "s" else ""} available.\n")
              false
            } else {
              true
            }
        }
      case _ =>
        println(s"Invalid Number of Tickets entered: $input.\n")
        false
    }
  }
}
