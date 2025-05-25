package org.rudi.code.cinema.booking.state.impl

import org.rudi.code.cinema.booking.constant.DataKey
import org.rudi.code.cinema.booking.state.State

class ExitState extends State {
  override def handle(): Map[DataKey, Any] = {
    println("\nThank you for using GIC Cinemas system. Bye!")
    Map.empty
  }
}
