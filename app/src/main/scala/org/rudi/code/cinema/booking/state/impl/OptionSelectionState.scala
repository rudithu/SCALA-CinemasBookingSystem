package org.rudi.code.cinema.booking.state.impl

import org.rudi.code.cinema.booking.constant.DataKey.SHOW_ID
import org.rudi.code.cinema.booking.constant.{AppConst, DataKey}
import org.rudi.code.cinema.booking.state.State
import org.rudi.code.cinema.booking.service.ShowService

import scala.collection.mutable
import scala.io.StdIn.readLine

class OptionSelectionState(showService: ShowService, sessionData: mutable.Map[DataKey, Any]) extends State {
  private val REGEX: String = "^([1-3]{1})$"
  private val pattern = REGEX.r

  private def prepShowData(): String = {




    val showId: Option[String] = getAsType[String](DataKey.SHOW_ID, sessionData)
    showId match {
      case Some(value) =>
        showService.getShow(value) match {
          case Some(show) =>
            val availableSeat = showService.getAvailableSeatCount(value)
            s"${show.movie.title} (${availableSeat} seat${if (availableSeat > 1) "s" else ""} available)"
          case None => "- (No Show Available)"
        }
      case None => "- (No Show Available)"
    }
  }

  override def handle(): Map[DataKey, Any] = {
    var input: String = ""
    var selectedOption: Option[Int] = None
    while (selectedOption.isEmpty) {
      println("\nWelcome to SCALA Cinemas")
      println(s"[1] Book ticket for ${prepShowData()}")
      println("[2] Check bookings")
      println("[3] Exit")
      println("Please enter your selection:")
      print(AppConst.INPUT_PROMPT)
      input = readLine().trim

      selectedOption = input match {
        case pattern(option) => Some(option.toInt)
        case _ => None
      }
    }
    Map(DataKey.OPTION_ID -> selectedOption.get)
  }
}
