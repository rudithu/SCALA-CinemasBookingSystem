package org.rudi.code.cinema.booking.util

import org.rudi.code.cinema.booking.constant.AppConst

import scala.util.matching.Regex

object BookingUtil {
  def getBookingIdCode(bookingId: String): Int = {
    val pattern: Regex = AppConst.BOOKING_ID_PATTERN.r
    bookingId match {
      case pattern(code) => code.toInt * -1
      case _ => throw new RuntimeException(s"Invalid Booking Id ${bookingId}")
    }
  }
}
