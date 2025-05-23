package org.rudi.code.cinema.booking

import org.rudi.code.cinema.booking.dao.{AuditoriumDAO, ShowDAO}
import org.rudi.code.cinema.booking.service.ShowService
import org.rudi.code.cinema.booking.state.impl.InitialSetupState

import scala.io.StdIn.readLine

object App {

  def main(args: Array[String]): Unit = {


    val showService = new ShowService(ShowDAO, AuditoriumDAO)
    val setup = new InitialSetupState(showService)

    println("Hello, scala gradle")
    println("===================")

    var input: String = ""
    while (!input.equalsIgnoreCase("exit")) {
      print("> ")
      input = readLine()
      println(s"your input $input")
    }
    println("bye. Thanks for Testing")
  }
}