package org.rudi.code.cinema.booking.state.impl

import org.rudi.code.cinema.booking.config.ConfigConstant
import org.rudi.code.cinema.booking.constant.{AppConst, DataKey}
import org.rudi.code.cinema.booking.service.ShowService
import org.rudi.code.cinema.booking.state.State

import scala.io.StdIn.readLine
import scala.collection.mutable

class InitialSetupState(showService: ShowService, sessionData: mutable.Map[DataKey, Any]) extends State {

  private val REGEX = "^(.*)\\s+(\\d+)\\s+(\\d+)\\s*$"
  private val pattern = REGEX.r

  override def handle(): Map[DataKey, Any] = {
    var input: String = ""
    var title: String = ""
    var rows: String = ""
    var seatsPerRow: String = ""
    var validated: Boolean = false

    while (!validated) {
      println("Please define movie title and seating map in [Title] [Row] [SeatsPerRow] format:")
      print(AppConst.INPUT_PROMPT)
      input = readLine()
      input match {
        case pattern(t, r, s) =>
          title = t
          rows = r
          seatsPerRow = s
          validated = validateInput(rows, seatsPerRow)
        case _ => validated = false
      }
      println()
    }

    val showId = showService.createShow(title, rows.toInt, seatsPerRow.toInt)
    sessionData.put(DataKey.SHOW_ID, showId)
    Map(DataKey.SHOW_ID -> showId)
  }

  private def validateInput(rowStr: String, colStr: String): Boolean = {
    val rows = rowStr.toInt
    val cols = colStr.toInt

    rows >= ConfigConstant.MIN_ROW && rows <= ConfigConstant.MAX_ROW
    && cols >= ConfigConstant.MIN_SEAT_PER_ROW && cols <= ConfigConstant.MAX_SEAT_PER_ROW
  }
}
