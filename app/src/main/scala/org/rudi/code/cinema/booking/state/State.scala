package org.rudi.code.cinema.booking.state

import org.rudi.code.cinema.booking.constant.DataKey

trait State {
  def handle(): Map[DataKey, Any]
}
