package org.rudi.code.cinema.booking.state.impl

import org.rudi.code.cinema.booking.constant.DataKey
import org.rudi.code.cinema.booking.state.State

class ExitState extends State {
  override def handle(): Map[DataKey, Any] = {
    println("Thank you for using GIC Cinemas system. Bye!\n")
    Map.empty
  }

}
