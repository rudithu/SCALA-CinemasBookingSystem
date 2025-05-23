package org.rudi.code.cinema.booking.dao

import org.rudi.code.cinema.booking.models.Booking

import java.util.concurrent.atomic.AtomicInteger
import scala.collection.mutable

object BookingDAO {

  private val bookingIndex = new AtomicInteger(0)
  private val bookingMap: mutable.Map[String, Booking] = mutable.Map.empty
  private val tempBookingMap: mutable.Map[String, Booking] = mutable.Map.empty


  def saveTempBooking(booking: Booking): Unit = tempBookingMap.put(booking.id, booking)

  def confirmBooking(bookingId: String): Unit = {
    tempBookingMap.get(bookingId).foreach(booking => {
      bookingMap.put(bookingId, booking)
      tempBookingMap.remove(bookingId)
    })
  }

  def findById(bookingId: String): Option[Booking] = bookingMap.get(bookingId)

  def findTempBookingById(bookingId: String): Option[Booking] = tempBookingMap.get(bookingId)

  def getNextBookingIndex: String =  f"SCALA${bookingIndex.incrementAndGet()}%04d"
}