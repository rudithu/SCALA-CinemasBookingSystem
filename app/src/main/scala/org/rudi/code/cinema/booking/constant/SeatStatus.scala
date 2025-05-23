package org.rudi.code.cinema.booking.constant

enum SeatStatus(val code: Int) {
  case AVAILABLE extends SeatStatus(0)
  case OCCUPIED extends SeatStatus(1)
  case LOCKED extends SeatStatus(2)
  case LOCKED_BY_OTHER extends SeatStatus(Integer.MIN_VALUE)
}

object SeatStatus {
  def isAvailable(code: Int): Boolean = code == SeatStatus.AVAILABLE.code
  def getSeatStatus(code: Int): Option[SeatStatus] = {
    SeatStatus.values.find(_.code == code).orElse(
      if (code < 0) Some(SeatStatus.LOCKED_BY_OTHER) else None
    )
  }
}
