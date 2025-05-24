package org.rudi.code.cinema.booking.service

import org.rudi.code.cinema.booking.dao.{AuditoriumDAO, BookingDAO, ShowDAO}
import org.rudi.code.cinema.booking.models.Booking
import org.rudi.code.cinema.booking.util.{BookingUtil, SeatUtil}
import org.rudi.code.cinema.booking.strategy.SeatAssignmentStrategy

class BookingService(seatAssignmentStrategy: SeatAssignmentStrategy, showDAO: ShowDAO.type, auditoriumDAO: AuditoriumDAO.type, bookingDAO: BookingDAO.type, bookingUtil: BookingUtil.type, seatUtil: SeatUtil.type) {

  def createTempBooking(showId: String, requiredSeats: Int): Booking = {
    val currentShow = showDAO.findById(showId).getOrElse(throw new RuntimeException(s"No show found for show id: ${showId}"))
    val auditoriumId = currentShow.auditoriumId

    val lock = auditoriumDAO.getOrCreateLock(auditoriumId)
    lock.acquire()
    try {
      val layoutCache = auditoriumDAO.getSeatLayoutCache(auditoriumId).getOrElse(throw new RuntimeException(s"No seat layout found for auditorium id: ${auditoriumId}"))
      val newBooking = Booking(id = bookingDAO.getNextBookingIndex, show = currentShow)

      val suggestedSeats = seatAssignmentStrategy.suggestSeats(newBooking.id, layoutCache, requiredSeats)
      auditoriumDAO.updateSeatLayoutCache(auditoriumId, suggestedSeats, newBooking.bookedSeatIndexes, bookingUtil.getBookingIdCode(newBooking.id))
      newBooking.bookedSeatIndexes = suggestedSeats
      bookingDAO.saveTempBooking(newBooking)

      newBooking
    } finally {
      lock.release()
    }
  }

  def changeSeats(tempBookingId: String, seatStartLoc: String): Booking = {
    val tempBooking: Booking = findTempBooking(tempBookingId).getOrElse(throw new RuntimeException(s"No temp-booking found for booking id: ${tempBookingId}"))
    val auditoriumId = tempBooking.show.auditoriumId
    val lock = auditoriumDAO.getOrCreateLock(auditoriumId)
    lock.acquire()
    try {
      val layoutCache = auditoriumDAO.getSeatLayoutCache(auditoriumId).getOrElse(throw new RuntimeException(s"No seat layout found for auditorium id: ${auditoriumId}"))
      val seatIndex = seatUtil.getSeatIndexFromString(seatStartLoc)
      val selectedSeat = seatAssignmentStrategy.selectSeats(tempBookingId, layoutCache, tempBooking.bookedSeatIndexes.size, seatIndex)
      auditoriumDAO.updateSeatLayoutCache(auditoriumId, selectedSeat, tempBooking.bookedSeatIndexes, BookingUtil.getBookingIdCode(tempBookingId))
      tempBooking.bookedSeatIndexes = selectedSeat
      tempBooking
    } finally {
      lock.release()
    }
  }

  def confirmSeats(tempBookingId: String): Unit = {
    val tempBooking: Booking = findTempBooking(tempBookingId).getOrElse(throw new RuntimeException(s"No temp-booking found for booking id: ${tempBookingId}"))
    val auditoriumId = tempBooking.show.auditoriumId
    val lock = auditoriumDAO.getOrCreateLock(auditoriumId)
    lock.acquire()
    try {
      bookingDAO.confirmBooking(bookingId = tempBookingId)
      auditoriumDAO.allocateAuditoriumSeats(auditoriumId = auditoriumId, seatIndexes = tempBooking.bookedSeatIndexes)
    } finally {
      lock.release()
    }
  }

  private def findTempBooking(bookingId: String): Option[Booking] = bookingDAO.findTempBookingById(bookingId)

  def findBooking(bookingId: String): Option[Booking] = bookingDAO.findById(bookingId)

}
