package org.rudi.code.cinema.booking.service

import org.rudi.code.cinema.booking.constant.SeatStatus
import org.rudi.code.cinema.booking.dao.{AuditoriumDAO, ShowDAO}
import org.rudi.code.cinema.booking.models.{Auditorium, Movie, Show}

class ShowService(showDAO: ShowDAO.type, auditoriumDAO: AuditoriumDAO.type ) {
  def createShow(movieTitle: String, seatRowNumber: Int, seatColNumber: Int):String = {
    val movie = Movie(movieTitle)
    val auditorium = Auditorium(seatRowNumber, seatColNumber)
    auditoriumDAO.saveAuditorium(auditorium)
    val show = Show(movie, auditorium.id)
    showDAO.save(show)
    show.id
  }

  def getShow(showId: String): Option[Show] = showDAO.findById(showId)

  def getAvailableSeatCount(showId: String): Int = {
    withAuditoriumLock(showId, show => {
      auditoriumDAO.getSeatLayoutCache(show.auditoriumId).getOrElse(
        throw new RuntimeException(s"No seat layout found for auditorium id: ${show.auditoriumId}")
      ).flatten.count(_ == SeatStatus.AVAILABLE.code)
    })
  }

  def getSeatsLayout(showId: String): Array[Array[Int]] = {
    withAuditoriumLock(showId, show => {
      auditoriumDAO.getSeatLayoutCache(show.auditoriumId)
        .getOrElse(throw new RuntimeException(s"No auditorium in the auditorium id ${show.auditoriumId}"))
    })
  }

  private def withAuditoriumLock[T](showId: String, f: Show => T): T = {
    val show = showDAO.findById(showId).getOrElse(
      throw new RuntimeException(s"No show found for show id: ${showId}")
    )
    val lock = auditoriumDAO.getOrCreateLock(auditoriumId = show.auditoriumId)
    lock.acquire()
    try {
      f(show)
    } finally {
      lock.release()
    }
  }

}
