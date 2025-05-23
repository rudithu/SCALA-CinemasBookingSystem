package org.rudi.code.cinema.booking.constant

import scala.reflect.ClassTag

enum DataKey(val clazz: Class[_]) {
  case SHOW_ID extends DataKey(classOf[String])
  case BOOKING_ID extends DataKey(classOf[String])
  case OPTION_ID extends DataKey(classOf[Int])

  def getValue[T](map: Map[DataKey, Any])(using ct: ClassTag[T]): Option[T] =
    map.get(this) match {
      case Some(value) if clazz.isInstance(value) =>
        Some(value.asInstanceOf[T])
      case _ =>
        None
    }

}
