package org.rudi.code.cinema.booking

import org.rudi.code.cinema.booking.constant.AppState.{BOOK_SEATS, CHECK_BOOKING, EXIT, MAIN_MENU, SETUP_MOVIE, START}
import org.rudi.code.cinema.booking.constant.DataKey.SHOW_ID
import org.rudi.code.cinema.booking.constant.{AppState, DataKey}
import org.rudi.code.cinema.booking.dao.{AuditoriumDAO, BookingDAO, ShowDAO}
import org.rudi.code.cinema.booking.service.{BookingService, ShowService}
import org.rudi.code.cinema.booking.state.impl.{BookingState, CheckingState, ExitState, InitialSetupState, OptionSelectionState}
import org.rudi.code.cinema.booking.strategy.SeatAssignmentStrategy
import org.rudi.code.cinema.booking.strategy.impl.KeepPossibleSameRowAndMiddle
import org.rudi.code.cinema.booking.util.{BookingUtil, LayoutUtil, SeatUtil}

import scala.annotation.tailrec
import scala.collection.immutable.ArraySeq.ofUnit
import scala.io.StdIn.readLine
import scala.collection.mutable

object App {

  def main(args: Array[String]): Unit = {


    val sessionData: mutable.Map[DataKey, Any] = mutable.Map.empty
    val seatAssignmentStrategy: SeatAssignmentStrategy = new KeepPossibleSameRowAndMiddle

    val showService = new ShowService(ShowDAO, AuditoriumDAO)
    val bookingService = new BookingService(seatAssignmentStrategy, ShowDAO, AuditoriumDAO, BookingDAO, BookingUtil, SeatUtil)

    val setupState = new InitialSetupState(showService, sessionData)
    val optionSelectionState = new OptionSelectionState(showService, sessionData)
    val bookingState = new BookingState(bookingService, showService, LayoutUtil, SeatUtil, sessionData)
    val checkingState = new CheckingState(bookingService, showService, LayoutUtil)
    val exitState = new ExitState

    @tailrec
    def stateFlow(state: AppState): Unit = {
      state match {
        case START => stateFlow(SETUP_MOVIE)
        case SETUP_MOVIE =>
          setupState.handle()
          stateFlow(MAIN_MENU)
        case MAIN_MENU =>
          val selection = optionSelectionState.handle()
          selection.get(DataKey.OPTION_ID) match {
            case Some(option: Int) => option match {
              case 1 => stateFlow(BOOK_SEATS)
              case 2 => stateFlow(CHECK_BOOKING)
              case 3 => stateFlow(EXIT)
              case _ => stateFlow(MAIN_MENU)
            }
            case _ => stateFlow(MAIN_MENU)
          }
        case BOOK_SEATS =>
          bookingState.handle()
          stateFlow(MAIN_MENU)
        case CHECK_BOOKING =>
          checkingState.handle()
          stateFlow(MAIN_MENU)
        case EXIT => exitState.handle()
      }
    }

    stateFlow(START)
  }

}