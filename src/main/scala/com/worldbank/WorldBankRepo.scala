package com.worldbank

import cats.effect._
import cats.implicits._
import com.worldbank.DBEntities.{DBCountryData, DBGDP, DBPopulation, DBPopulationData}
import com.worldbank.IngestionEntities.{CountryData, GDP, Population}
import com.worldbank.QueryEntities.QueryResult
import doobie.{Fragments, _}
import Fragments.{ in, whereAndOpt }
import doobie.implicits._
import doobie.util.ExecutionContexts


trait WorldBankRepo[F[_]] {
  def saveCountryData(data: List[CountryData]): F[Int]
  def savePopulationData(populationData: List[Population]): F[Int]
  def saveGDPData(gdpData: List[GDP]): F[Int]
  def top10PopulationGrowth(): F[List[QueryResult]]
  def top3GDBGrowth(countryList: List[String]): F[List[QueryResult]]
}

object WorldBankRepo {
  def impl[F[_]: Concurrent](xa: Transactor[F]) = new WorldBankRepo[F] {

    override def saveCountryData(countryData: List[CountryData]): F[Int] = {
      val sql = "insert into countries (countryiso3code, name, capitalCity, latitude, longitude) values (?, ?, ?, ?, ?)"
      val trx = Update[DBCountryData](sql).updateMany(countryData.map(DBEntities.toDBCountryData))
      trx.transact(xa).recoverWith { error =>
        Concurrent[F].raiseError(new RuntimeException(error.getMessage))
      }
    }

    override def savePopulationData(populationData: List[Population]): F[Int] = {
      val sql = "insert into population (countryiso3code, value, year, country) values (?, ?, ?, ?)"
      val trx = Update[DBPopulation](sql).updateMany(populationData.map(DBEntities.toDBPopulation))
      trx.transact(xa).recoverWith{ _ =>
        Concurrent[F].pure(0)
      }
    }

    override def saveGDPData(gdpData: List[GDP]): F[Int] = {
      val sql = "insert into gdp (countryiso3code, value, year, country) values (?, ?, ?, ?)"
      val trx = Update[DBGDP](sql).updateMany(gdpData.map(DBEntities.toDBGDP))
      trx.transact(xa).recoverWith{ _ =>
        Concurrent[F].pure(0)
      }
    }

    override def top10PopulationGrowth(): F[List[QueryResult]] = {
      val q =
        sql"""
          select countryiso3code, sum(value) as grow from population
        where countryiso3code = countryiso3code
          and countryiso3code NOT IN (select countryiso3code from countries where capitalCity = '')
          and `year` between 2010 and 2018 and countryiso3code is not '' group by countryiso3code order by grow desc
        limit 10
        """
      val result = q.query[QueryResult].to[List].transact(xa)

      result
    }

    override def top3GDBGrowth(countryList: List[String]): F[List[QueryResult]] = {
      val inFragment = fr"and p1.countryiso3code  IN (" ++ countryList.map(c => fr"$c").intercalate(fr",") ++ fr")"
      val q:Fragment =
        fr"""
          select p1.countryiso3code, (100.0 * (p2.value - p1.value) / p1.value ) as growth
          from gdp as p1
          inner join gdp as p2
            on p1.countryiso3code = p2.countryiso3code and p1.countryiso3code <> ''""" ++ inFragment ++ fr""" order by growth desc limit 3;"""

      q.query[QueryResult].to[List].transact(xa).recoverWith{
        error =>
          println(error.toString)
          Concurrent[F].pure(List())
      }
    }
  }
}
