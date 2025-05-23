package org.rudi.code.cinema.booking.util

import org.rudi.code.cinema.booking.constant.{AppConst, SeatStatus}

import java.util
import java.util.List
import java.util.regex.{Matcher, Pattern}
import scala.collection.mutable
object SeatUtil {
  
  def prepSeatRanges(row: Array[Int], bookingCode: Int): mutable.ListBuffer[Array[Int]] = {
    val seatRange: mutable.ListBuffer[Array[Int]] = mutable.ListBuffer.empty
    row.length match {
      case 0 => seatRange
      case totalSeatInRow =>
        var rangeStart: Int = -1
        
        for (i <- 0 until totalSeatInRow) {
          if ((rangeStart == -1 || i == 0) && (SeatStatus.isAvailable(row(i)) || row(i) == bookingCode)) {
            rangeStart = i
          } else if (rangeStart != -1 && !SeatStatus.isAvailable(row(i)) && row(i) != bookingCode) {
            seatRange.addOne(Array(rangeStart, i-1))
            rangeStart = -1
          }
        }
        if (rangeStart != -1) {
          seatRange.addOne(Array(rangeStart, totalSeatInRow - 1))
        }
        seatRange
    }
  }
  
  def getSeatIndex(row: Int, colRanges: mutable.ListBuffer[Array[Int]]): mutable.ListBuffer[Array[Int]] = {
    val result: mutable.ListBuffer[Array[Int]] = mutable.ListBuffer.empty
    for (cols <- colRanges) {
      for (col <- 0 to cols(1)) {
        result.addOne(Array(row, col))
      }
    }
    result
  }
  
  def mergeAvailableSeatMap(seatRanges:mutable.ListBuffer[Array[Int]], acquiredSeatRange: Array[Int]): mutable.ListBuffer[Array[Int]] = {
    val updatedSeatRange: mutable.ListBuffer[Array[Int]] = mutable.ListBuffer.empty
    for (seats <- seatRanges) {
      if (seats(0) <= acquiredSeatRange(0) && seats(1) >= acquiredSeatRange(1)) {
        if (seats(0) != acquiredSeatRange(0) || seats(1) != acquiredSeatRange(1)) {
          if (seats(0) < acquiredSeatRange(0)) {
            updatedSeatRange.addOne(Array(seats(0), acquiredSeatRange(0) - 1))
          }
          if (seats(1) > acquiredSeatRange(1)) {
            updatedSeatRange.addOne(Array(acquiredSeatRange(1) - 1, seats(1)))
          }
        }
      } else {
        updatedSeatRange.addOne(seats)
      }
    }
    updatedSeatRange
  }


  private def convertRowIndex(row: String): Int = {
    val rowIndexes = row.toUpperCase.toCharArray
    var count: Int = 0
    for (c <- rowIndexes) {
      count *= 26
      count += c - 'A' + 1
    }
    count - 1
  }

  def getSeatIndexFromString(seatNumber: String): Array[Int] =
    seatNumber match {
      case AppConst.SEAT_INDEX_PATTERN.r(r, c) => Array(convertRowIndex(r), c.toInt)
      case _ => throw new RuntimeException(s"Unexpected seat number ${seatNumber}")
    }
  
}
