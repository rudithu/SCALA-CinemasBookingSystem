package org.rudi.code.cinema.booking.dao

import org.rudi.code.cinema.booking.constant.SeatStatus
import org.rudi.code.cinema.booking.models.Auditorium

import java.util.concurrent.Semaphore
import scala.collection.{mutable, immutable}

object AuditoriumDAO {

  private val auditoriumMap: mutable.Map[String, Auditorium] = mutable.Map.empty
  private val seatsCache: mutable.Map[String, Array[Array[Int]]] = mutable.Map.empty
  private val semaphoreMap: mutable.Map[String, Semaphore] = mutable.Map.empty

  def getOrCreateLock(auditoriumId: String): Semaphore = {
    semaphoreMap.synchronized {
      semaphoreMap.getOrElseUpdate(auditoriumId, new Semaphore(1))
    }
  }

  def saveAuditorium(auditorium: Auditorium): Unit = {
    auditoriumMap.put(auditorium.id, auditorium)
    val copiedSeats = auditorium.seats.map(_.clone())
    seatsCache.put(auditorium.id, copiedSeats)
  }

  def findById(auditoriumId: String): Option[Auditorium] = auditoriumMap.get(auditoriumId)

  def getSeatLayoutCache(auditoriumId: String): Option[Array[Array[Int]]] = seatsCache.get(auditoriumId)

  def updateSeatLayoutCache(auditoriumId: String, indexesAdd: mutable.ListBuffer[Array[Int]], indexesRemove: mutable.ListBuffer[Array[Int]], lockedCode: Int): Unit = {

    val indexes = scala.collection.mutable.Set[String]()
    val cache = getSeatLayoutCache(auditoriumId).getOrElse(throw new RuntimeException(s"No seat layout found for auditorium id: ${auditoriumId}"))

    Option(indexesRemove).foreach { removeList => removeList.foreach { index => {
        val key = s"${index(0)}_${index(1)}"
        if (indexes.add(key)) {
          cache(index(0))(index(1)) = SeatStatus.AVAILABLE.code
        }
      }}
    }

    indexes.clear()
    Option(indexesAdd).foreach { addList => addList.foreach { index => {
        val key = s"${index(0)}_${index(1)}"
        if (indexes.add(key)) {
          cache(index(0))(index(1)) = lockedCode
        }
      }}
    }
  }

  def allocateAuditoriumSeats(auditoriumId: String, seatIndexes: mutable.ListBuffer[Array[Int]]): Unit = {
    val auditorium = findById(auditoriumId).getOrElse(throw new RuntimeException(s"No auditorium foud for auditorium id: $auditoriumId"))

    val seatLayout = auditorium.seats
    val cache = getSeatLayoutCache(auditoriumId).getOrElse(throw new RuntimeException(s"No seat layout found for auditorium id: ${auditoriumId}"))

    val set = scala.collection.mutable.Set[String]()
    Option(seatIndexes).foreach { seatIndexes => {seatIndexes.foreach { seatIndex => {
        if (seatIndex.length != 2) throw new RuntimeException("Invalid Seat Index")
        if (set.add(s"${seatIndex(0)}_${seatIndex(1)}")) {
          seatLayout(seatIndex(0))(seatIndex(1)) = SeatStatus.OCCUPIED.code
          cache(seatIndex(0))(seatIndex(1)) = SeatStatus.OCCUPIED.code
        }
      }}}}
  }
}
