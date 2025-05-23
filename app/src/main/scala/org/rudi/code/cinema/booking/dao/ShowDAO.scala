package org.rudi.code.cinema.booking.dao

import org.rudi.code.cinema.booking.models.Show

import scala.collection.mutable

object ShowDAO {
  private val showMap: mutable.Map[String, Show] = mutable.Map.empty

  def save(show: Show): Unit = {
    showMap.put(show.id, show)
  }

  def findById(showId: String): Option[Show] = showMap.get(showId)

}
