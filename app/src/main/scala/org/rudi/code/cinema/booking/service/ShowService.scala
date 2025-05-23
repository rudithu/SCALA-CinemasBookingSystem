package org.rudi.code.cinema.booking.service

import org.rudi.code.cinema.booking.constant.SeatStatus
import org.rudi.code.cinema.booking.dao.{AuditoriumDAO, ShowDAO}
import org.rudi.code.cinema.booking.models.{Auditorium, Movie, Show}

class ShowService(showDAO: ShowDAO.type, auditoriumDAO: AuditoriumDAO.type ) {
  def createShow(movieTitle: String, seatRowNumber: Int, seatColNumber: Int):String = {
    val movie = Movie(movieTitle)
    val auditorium = Auditorium(seatColNumber, seatColNumber)
    val show = Show(movie, auditorium.id)
    showDAO.save(show)
    show.id
  }

  def getShow(showId: String): Option[Show] = showDAO.findById(showId)

  def getAvailableSeatCount(showId: String): Int = {
    val show = showDAO.findById(showId).getOrElse(
      throw new RuntimeException(s"No show found for show id: ${showId}")
    )

    val lock = auditoriumDAO.getOrCreateLock(auditoriumId = show.auditoriumId)
    lock.acquire()
    try {
      val seatLayout = auditoriumDAO.getSeatLayoutCache(show.auditoriumId).getOrElse(
        throw new RuntimeException(s"No seat layout found for auditorium id: ${show.auditoriumId}")
      )
      seatLayout.flatten.count(_ == SeatStatus.AVAILABLE.code)
    } finally {
      lock.release()
    }
  }
}
