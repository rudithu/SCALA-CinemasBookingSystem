package org.rudi.code.cinema.booking.state.impl

import org.rudi.code.cinema.booking.config.ConfigConstant
import org.rudi.code.cinema.booking.constant.{AppConst, DataKey}
import org.rudi.code.cinema.booking.service.ShowService
import org.rudi.code.cinema.booking.state.State

import scala.annotation.tailrec
import scala.io.StdIn.readLine
import scala.collection.mutable

class InitialSetupState(showService: ShowService, sessionData: mutable.Map[DataKey, Any]) extends State {

  private val REGEX = "^(.*)\\s+(\\d+)\\s+(\\d+)\\s*$"
  private val pattern = REGEX.r

  override def handle(): Map[DataKey, Any] = {
    @tailrec
    def prompt: Map[DataKey, Any] = {
      println("\nPlease define movie title and seating map in [Title] [Row] [SeatsPerRow] format:")
      print(AppConst.INPUT_PROMPT)
      val input = readLine().trim
      input match {
        case pattern(t, r, s) =>
          if (validateInput(r, s)) {
            val showId = showService.createShow(t, r.toInt, s.toInt)
            sessionData.put(DataKey.SHOW_ID, showId)
            Map(DataKey.SHOW_ID -> showId)
          } else {
            prompt
          }
        case _ => prompt
      }
    }
    prompt
  }

  private def validateInput(rowStr: String, colStr: String): Boolean = {
    val rows = rowStr.toInt
    val cols = colStr.toInt

    rows >= ConfigConstant.MIN_ROW && rows <= ConfigConstant.MAX_ROW
    && cols >= ConfigConstant.MIN_SEAT_PER_ROW && cols <= ConfigConstant.MAX_SEAT_PER_ROW
  }
}
