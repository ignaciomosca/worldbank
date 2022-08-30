package com.worldbank

import cats.effect._
import cats.implicits._
import com.worldbank.DBEntities.{DBGDP, DBPopulation, DBPopulationData}
import com.worldbank.IngestionEntities.{GDP, Population}
import com.worldbank.QueryEntities.QueryResult
import doobie._
import doobie.implicits._

import scala.collection.immutable.List

trait WorldBankRepo[F[_]] {
  def savePopulationData(populationData: List[Population]): F[Int]
  def saveGDPData(gdpData: List[GDP]): F[Int]
  def top10PopulationGrowth(): F[List[QueryResult]]
  def top3GDBGrowth(): F[List[QueryResult]]
}

object WorldBankRepo {
  def impl[F[_]: Concurrent](xa: Transactor[F]) = new WorldBankRepo[F] {

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
          select p1.countryiso3code, (100.0 * (p2.value - p1.value) / p1.value ) as growth
          from population as p1
          inner join population as p2
            on p1.countryiso3code = p2.countryiso3code and p1.year = 2010 and p2.year = 2018 and p1.countryiso3code <> ''
          order by growth desc
          limit 10;
        """

      for {
        r <- q.query[QueryResult].to[List].transact(xa)
        _ <- saveToPopulationResultData(r)
      } yield(r)
    }

    override def top3GDBGrowth(): F[List[QueryResult]] = {
      val q =
        sql"""
          select p1.countryiso3code, (100.0 * (p2.value - p1.value) / p1.value ) as growth
          from gdp as p1
          inner join gdp as p2
            on p1.countryiso3code = p2.countryiso3code and p1.year = 2010 and p2.year = 2018 and p1.countryiso3code <> '' and p1.countryiso3code in (
              select countryiso3code from populationresult)
          order by growth desc
          limit 3;
        """
      q.query[QueryResult].to[List].transact(xa).recoverWith{
        _ =>
          Concurrent[F].pure(List())
      }
    }

    private def saveToPopulationResultData(data: List[QueryResult]) = {
      val sql = "insert into populationresult (countryiso3code) values (?)"
      val trx = Update[DBPopulationData](sql).updateMany(data.map(_.countryiso3code).map(DBPopulationData))
      val result = trx.transact(xa).recoverWith{ e =>
        Concurrent[F].pure(0)
      }
      result.map(println(_))
      result
    }


  }
}