package com.worldbank

import cats.effect._
import cats.effect.unsafe.implicits.global
import doobie.util.transactor.Transactor

object Main extends IOApp {
  def run(args: List[String]) = {
    val xa = Transactor.fromDriverManager[IO]("org.sqlite.JDBC", "jdbc:sqlite:worldbank.db", "", "")
    if(args.headOption.exists(_.equals("resetdatabase"))) {
      println("Resetting database")
      InitializeDatabase.initialize[IO](xa).unsafeRunSync()
    }
    for {
      _ <-WorldBankServer.process[IO](args.contains("ingestion"), xa)
    } yield ExitCode.Success
  }
}
