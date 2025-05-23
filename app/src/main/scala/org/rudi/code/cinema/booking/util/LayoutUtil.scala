package org.rudi.code.cinema.booking.util

import scala.collection.mutable

import org.rudi.code.cinema.booking.constant.SeatStatus

object LayoutUtil {

  private val FIRST_PADDING: Int = 2
  private val CELL_PADDING: Int = 4
  private val MIN_SIZE: Int = 6
  private val SPACE: Char = ' '

  def generateLayout(layout: Array[Array[Int]], lockedSeats: mutable.ListBuffer[Array[Int]]): mutable.ListBuffer[String] = {
    val totalRow = layout.length
    val totalCol = layout(0).length

    val lockedSeatsMap: mutable.Map[Int, mutable.Set[Int]] = mutable.Map.empty
    for (seat <- lockedSeats) {
      if (!lockedSeatsMap.contains(seat(0))) {
        lockedSeatsMap.put(seat(0), mutable.Set.empty)
      }
      lockedSeatsMap.get(seat(0)).foreach(set => set.add(seat(1)))
    }

    val layoutRows: mutable.ListBuffer[String] = mutable.ListBuffer.empty
    layoutRows.addOne(generateScreen(totalCol))
    layoutRows.addOne(generateScreenLine(totalCol))
    for (i <- (0 until totalRow).reverse) {
      layoutRows.addOne(generateSeatRow(i, layout(i), lockedSeatsMap.get(i)))
    }
    layoutRows.addOne(generateSeatColNumber(totalCol))
    layoutRows
  }

  private def generateSeatColNumber(totalCol: Int): String = {
    val sb = new StringBuilder(padRight("", SPACE, FIRST_PADDING))
    for (i <- 1 to totalCol) {
      sb.append(padRight(i.toString, SPACE, CELL_PADDING))
    }
    if (sb.length < MIN_SIZE) {
      sb.append(padRight("", SPACE, MIN_SIZE - sb.length()))
    }
    sb.toString()
  }

  private def generateScreen(totalCol: Int): String = {
    var screen: String = "S C R E E N"
    val size = math.max(FIRST_PADDING + totalCol * CELL_PADDING, MIN_SIZE)
    if (size <= screen.length) {
      screen = "SCREEN"
    }
    val leftSide = (size - screen.length) / 2
    padRight("", SPACE, leftSide) + padRight(screen, SPACE, size - leftSide)
  }

  private def generateScreenLine(totalCol: Int): String =
    padRight("", '-', math.max(FIRST_PADDING + totalCol * CELL_PADDING, MIN_SIZE))

  private def generateSeatRow(row: Int, values: Array[Int], reserveSeatOpt: Option[mutable.Set[Int]]): String = {
    val charRow: Char = ('A' + row).toChar
    val sb = new StringBuilder(padRight(charRow.toString, SPACE, FIRST_PADDING))
    var bb: Boolean = reserveSeatOpt.exists(set => set.contains(2))
    for (i <- values.indices) {
      sb.append(padRight(
        input = mapValue(input = values(i), reserved = reserveSeatOpt.exists(set => set.contains(i))),
        padding = SPACE,
        size = CELL_PADDING)
      )
    }
    if (sb.length < MIN_SIZE) {
      sb.append(padRight("", SPACE, MIN_SIZE - sb.length()))
    }
    sb.toString()
  }

  private def mapValue(input: Int, reserved: Boolean): String =
    (if (reserved) {
      Some(SeatStatus.LOCKED)
    } else {
      SeatStatus.getSeatStatus(input)
    }) match {
      case Some(SeatStatus.AVAILABLE) => "."
      case Some(SeatStatus.OCCUPIED) => "#"
      case Some(SeatStatus.LOCKED) => "o"
      case _ => "x"
    }

  private def padRight(input: String, padding: Char, size: Int): String = input + padding.toString * math.max(size - input.length, 0)

}
