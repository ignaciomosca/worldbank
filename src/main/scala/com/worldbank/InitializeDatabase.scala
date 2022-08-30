package com.worldbank

import cats.effect._
import cats.implicits._
import doobie.implicits._
import doobie.util.transactor.Transactor

object InitializeDatabase {
  def initialize[F[_]: Async](xa: Transactor[F]) = {

    val dropPopulation = sql"""DROP TABLE IF EXISTS population""".update

    val dropGDP = sql"""DROP TABLE IF EXISTS gdp""".update

    val droppopulationresult = sql"""DROP TABLE IF EXISTS populationresult""".update

    val createPopulationTable =
      sql"""
    CREATE TABLE IF NOT EXISTS population(
   countryiso3code           TEXT    NOT NULL,
   value            INT,
   year        TEXT,
   country         TEXT);
  """.update

    val createGDPTable =
      sql"""
    CREATE TABLE IF NOT EXISTS gdp(
   countryiso3code           TEXT    NOT NULL,
   value            NUMERIC,
   year        INT,
   country         TEXT);
  """.update

    val createPopulationResultTable =
      sql"""
    CREATE TABLE IF NOT EXISTS populationresult(
   countryiso3code           TEXT    NOT NULL);
  """.update

    for {
      drop0 <- droppopulationresult.run.transact(xa)
      drop1 <- dropPopulation.run.transact(xa)
      drop2 <- dropGDP.run.transact(xa)
      result0 <- createPopulationTable.run.transact(xa)
      result1 <- createPopulationResultTable.run.transact(xa)
      result2 <- createGDPTable.run.transact(xa)
    } yield (drop0, drop1, drop2, result0, result1, result2)
  }
}
