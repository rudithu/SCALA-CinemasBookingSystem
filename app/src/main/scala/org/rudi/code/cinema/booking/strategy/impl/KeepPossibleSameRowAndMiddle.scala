package org.rudi.code.cinema.booking.strategy.impl

import org.rudi.code.cinema.booking.strategy.SeatAssignmentStrategy
import org.rudi.code.cinema.booking.util.BookingUtil
import org.rudi.code.cinema.booking.util.SeatUtil.*

import scala.annotation.targetName
import scala.collection.mutable
import scala.util.control.Breaks.*

class KeepPossibleSameRowAndMiddle extends SeatAssignmentStrategy {

  override def suggestSeats(bookingId: String, seatLayout: Array[Array[Int]], requiredSeats: Int): mutable.ListBuffer[Array[Int]] =
    suggestSeats(bookingId = bookingId, seatLayout = seatLayout, requiredSeats = requiredSeats, startingRow = 0, seatRangesRowMap = mutable.Map.empty)

  override def selectSeats(bookingId: String, seatLayout: Array[Array[Int]], requiredSeats: Int, initSeatIndex: Array[Int]): mutable.ListBuffer[Array[Int]] = {
    val row = seatLayout.length
    val seatRangesRowMap: mutable.Map[Integer, mutable.ListBuffer[Array[Int]]] = mutable.Map.empty
    val result: mutable.ListBuffer[Array[Int]] = mutable.ListBuffer.empty
    var seatToAcquire: Int = requiredSeats

    var seatRangesList = prepSeatRanges(seatLayout(initSeatIndex(0)), BookingUtil.getBookingIdCode(bookingId))
    val selectedSeatRangesResult = selectRowSeats(seatRangesList, seatToAcquire, initSeatIndex(1))

    seatToAcquire -= selectedSeatRangesResult.map(s => s(1) - s(0) + 1).sum
    result.addAll(getSeatIndex(initSeatIndex(0), selectedSeatRangesResult))

    if (seatToAcquire > 0) {
      for (currentResult <- selectedSeatRangesResult) {
        seatRangesList = mergeAvailableSeatMap(seatRangesList, currentResult)
      }
      seatRangesRowMap.put(initSeatIndex(0), seatRangesList)
      result.addAll(suggestSeats(bookingId = bookingId, seatLayout = seatLayout, requiredSeats = seatToAcquire, startingRow = initSeatIndex(0) + 1, seatRangesRowMap = seatRangesRowMap))
    }
    result
  }

  @targetName("suggestSeatsWithMap")
  private def suggestSeats(bookingId: String, seatLayout: Array[Array[Int]], requiredSeats: Int, startingRow: Int, seatRangesRowMap: mutable.Map[Integer, mutable.ListBuffer[Array[Int]]]): mutable.ListBuffer[Array[Int]] = {
    val row = seatLayout.length
    val col = seatLayout(0).length
    val result: mutable.ListBuffer[Array[Int]] = mutable.ListBuffer.empty

    var seatToAcquire = requiredSeats

    (0 until row)
      .iterator
      .takeWhile(_ => seatToAcquire > 0)
      .foreach(index => {

        val rowIndex = (index + startingRow) % row
        val seatRangeList: mutable.ListBuffer[Array[Int]] = seatRangesRowMap.getOrElse(rowIndex, prepSeatRanges(seatLayout(rowIndex), BookingUtil.getBookingIdCode(bookingId)))
        val acquiredSeatRange = suggestRowSeats(seatRanges = seatRangeList, requiredSeats = seatToAcquire, totalCol = col)

        acquiredSeatRange.foreach(asr => {
          result.addAll(getSeatIndex(rowIndex, mutable.ListBuffer(asr)))
          seatRangesRowMap.put(rowIndex, mergeAvailableSeatMap(seatRangeList, asr))
          seatToAcquire -= (asr(1) - asr(0) + 1)
        })

      })

    (0 until row)
      .iterator
      .takeWhile(_ => seatToAcquire > 0)
      .foreach(index => {

        val rowIndex = (index + startingRow) % row
        val seatRangeList: mutable.ListBuffer[Array[Int]] = seatRangesRowMap.getOrElse(rowIndex, prepSeatRanges(seatLayout(rowIndex), BookingUtil.getBookingIdCode(bookingId)))
        val fillAnySeatRangesResult = fillInAnySeats(seatRangeList, seatToAcquire, col)

        seatToAcquire -= fillAnySeatRangesResult.map(s => s(1) - s(0)).sum
        result.addAll(getSeatIndex(rowIndex, fillAnySeatRangesResult))

      })

    result
  }

  private def suggestRowSeats(seatRanges: mutable.ListBuffer[Array[Int]], requiredSeats: Int, totalCol: Int): Option[Array[Int]] = {
    seatRanges.length match {
      case 0 => None
      case size =>
        val bestMiddleSeatRangeStart = if (totalCol >= requiredSeats) (totalCol - requiredSeats) / 2 else 0
        var bestSelectedSeatRange: Option[Array[Int]] = None
        breakable {
          for (i <- (size - 1) to 0 by -1) {
            val currentSeatRange = seatRanges(i)
            val space = currentSeatRange(1) - currentSeatRange(0) + 1

            if (space >= requiredSeats) {
              if (bestMiddleSeatRangeStart >= currentSeatRange(0) &&
                bestMiddleSeatRangeStart + requiredSeats - 1 <= currentSeatRange(1)) {
                bestSelectedSeatRange = Some(Array(bestMiddleSeatRangeStart, bestMiddleSeatRangeStart + requiredSeats - 1))
                break()
              }

              if (currentSeatRange(0) < bestMiddleSeatRangeStart) {
                var start = currentSeatRange(0)
                while (start + requiredSeats - 1 <= currentSeatRange(1)) {
                  if (
                    bestSelectedSeatRange.isEmpty ||
                      math.abs(bestSelectedSeatRange.get(0) - bestMiddleSeatRangeStart) > math.abs(start - bestMiddleSeatRangeStart) ||
                      (bestSelectedSeatRange.get(1) - bestSelectedSeatRange.get(0) + 1) < requiredSeats
                  ) {
                    bestSelectedSeatRange = Some(Array(start, start + requiredSeats - 1))
                  }
                  start += 1
                }
              } else {
                bestSelectedSeatRange = Some(Array(currentSeatRange(0), currentSeatRange(0) + requiredSeats - 1))
              }
            } else if (i == size - 1) {
              bestSelectedSeatRange = Some(currentSeatRange)
            }
          }
        }
        bestSelectedSeatRange
    }
  }

  private def fillInAnySeats(seatRanges: mutable.ListBuffer[Array[Int]], requireSeats: Int, totalCol: Int): mutable.ListBuffer[Array[Int]] = {
    val result: mutable.ListBuffer[Array[Int]] = mutable.ListBuffer.empty
    val pq: mutable.PriorityQueue[Int] = mutable.PriorityQueue.empty

    seatRanges.foreach(seatRange => {
      pq.addOne(seatRange(1) - seatRange(0))
    })
    val currentSeatRanges: mutable.ListBuffer[Array[Int]] = seatRanges
    var seatToAcquired = requireSeats
    breakable {
      while (pq.nonEmpty) {
        suggestRowSeats(currentSeatRanges, Math.min(pq.dequeue(), seatToAcquired), totalCol) match {
          case None => break()
          case theSeats =>
            result.addOne(theSeats.get)
            seatToAcquired -= (theSeats.get(1) - theSeats.get(0) + 1)
        }
      }
    }
    result
  }

  private def selectRowSeats(seatRanges: mutable.ListBuffer[Array[Int]], requiredSeats: Int, selectedCol: Int): mutable.ListBuffer[Array[Int]] = {
    val result: mutable.ListBuffer[Array[Int]] = mutable.ListBuffer.empty
    var seatToAcquire = requiredSeats

    val iter = seatRanges.iterator
    while (iter.hasNext && seatToAcquire > 0) {
      val seatRange = iter.next()
      if (seatRange(1) >= selectedCol) {
        val start = Math.max(selectedCol, seatRange(0))
        val end = Math.min(start + seatToAcquire - 1, seatRange(1))
        result.addOne(Array(start, end))
        seatToAcquire -= (end - start + 1)
      }

    }
    result
  }
}
