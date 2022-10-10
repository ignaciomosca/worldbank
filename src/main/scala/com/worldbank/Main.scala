package com.worldbank

import cats.effect._
import cats.effect.unsafe.implicits.global
import doobie.util.transactor.Transactor

object Main extends IOApp {
  def run(args: List[String]) = {
    for {
      config <- ServerConfig.load()
      xa = Transactor.fromDriverManager[IO]("org.sqlite.JDBC", config.url, config.username, config.pass)
      _ = if(args.headOption.exists(_.equals("resetdatabase"))) {InitializeDatabase.initialize[IO](xa).unsafeRunSync()} else { () }
      _<-WorldBankServer.process[IO](args.contains("ingestion"), xa)
    } yield ExitCode.Success
  }
}