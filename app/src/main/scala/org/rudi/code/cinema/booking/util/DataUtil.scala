package org.rudi.code.cinema.booking.util

import org.rudi.code.cinema.booking.constant.DataKey

import scala.collection.mutable
import scala.reflect.ClassTag

object DataUtil {


  def getAsType[T: ClassTag](key: DataKey, map: mutable.Map[DataKey, Any]): Option[T] = {
    map.get(key) match {
      case Some(value: T) => Some(value)
      case _ => None
    }
  }
}
