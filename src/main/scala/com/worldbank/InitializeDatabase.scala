package com.worldbank

import cats.effect._
import cats.implicits._
import doobie.implicits._
import doobie.util.transactor.Transactor

object InitializeDatabase {
  def initialize[F[_]: Async](xa: Transactor[F]) = {
    println("Resetting database")

    val dropPopulation = sql"""DROP TABLE IF EXISTS population""".update

    val dropGDP = sql"""DROP TABLE IF EXISTS gdp""".update

    val dropCountries = sql"""DROP TABLE IF EXISTS countries""".update

    val createPopulationTable =
      sql"""
    CREATE TABLE IF NOT EXISTS population(
   countryiso3code           TEXT    NOT NULL,
   value            INT,
   year        TEXT,
   country         TEXT);
  """.update

    val createCountriesTable =
      sql"""
    CREATE TABLE IF NOT EXISTS countries(
   countryiso3code           TEXT    NOT NULL,
   name            TEXT NOT NULL,
   capitalCity        TEXT,
   latitude        TEXT,
   longitude         TEXT);
  """.update

    val createGDPTable =
      sql"""
    CREATE TABLE IF NOT EXISTS gdp(
   countryiso3code           TEXT    NOT NULL,
   value            NUMERIC,
   year        INT,
   country         TEXT);
  """.update

    for {
      dropPopulation <- dropPopulation.run.transact(xa)
      dropGDP <- dropGDP.run.transact(xa)
      dropCountries <- dropCountries.run.transact(xa)
      createPopulationTable <- createPopulationTable.run.transact(xa)
      createGDPTable <- createGDPTable.run.transact(xa)
      createCountriesTable <- createCountriesTable.run.transact(xa)
    } yield (dropPopulation, dropGDP, dropCountries, createPopulationTable, createGDPTable, createCountriesTable)
  }
}
