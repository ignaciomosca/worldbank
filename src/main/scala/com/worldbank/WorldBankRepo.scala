package com.worldbank

import cats.effect._
import cats.implicits._
import com.worldbank.DBEntities.{DBCountryData, DBGDP, DBPopulation, DBPopulationData}
import com.worldbank.IngestionEntities.{CountryData, GDP, Population}
import com.worldbank.QueryEntities.QueryResult
import doobie.{Fragments, _}
import Fragments.{in, whereAndOpt}
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
  def impl[F[_] : Concurrent](xa: Transactor[F]) = new WorldBankRepo[F] {

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
      trx.transact(xa).recoverWith { _ =>
        Concurrent[F].pure(0)
      }
    }

    override def saveGDPData(gdpData: List[GDP]): F[Int] = {
      val sql = "insert into gdp (countryiso3code, value, year, country) values (?, ?, ?, ?)"
      val trx = Update[DBGDP](sql).updateMany(gdpData.map(DBEntities.toDBGDP))
      trx.transact(xa).recoverWith { _ =>
        Concurrent[F].pure(0)
      }
    }

    override def top10PopulationGrowth(): F[List[QueryResult]] = {
      val q =
        sql"""
          WITH yearly_growth AS (
            SELECT
                p1.countryiso3code,
                p1.year,
                p1.value - COALESCE(p2.value, 0) AS growth
            FROM
                population p1
                    LEFT JOIN population p2 ON p1.countryiso3code = p2.countryiso3code
                    AND p1.year = CAST(p2.year AS INTEGER) + 1
            WHERE
                p1.year BETWEEN '2011' AND '2018'
        )
        SELECT
            yg.countryiso3code AS country,
            SUM(yg.growth) AS total_growth
        FROM
            yearly_growth yg
                JOIN countries c ON yg.countryiso3code = c.countryiso3code
        GROUP BY
            yg.countryiso3code
        ORDER BY
            total_growth DESC
        LIMIT 10;
        """
      val result = q.query[QueryResult].to[List].transact(xa)

      result
    }

    override def top3GDBGrowth(countryList: List[String]): F[List[QueryResult]] = {
      val inFragment = fr"(" ++ countryList.map(c => fr"$c").intercalate(fr",") ++ fr")"
      val q: Fragment =
        fr"""
         WITH gdp_growth AS (
            -- Calculate GDP growth for the highest growth countries
            SELECT
                g1.countryiso3code,
                g1.year,
                g1.value - COALESCE(g2.value, 0) AS growth
            FROM
                gdp g1
                LEFT JOIN gdp g2 ON g1.countryiso3code = g2.countryiso3code
                                    AND g1.year = g2.year + 1
            WHERE
                g1.year BETWEEN 2011 AND 2018
                AND g1.countryiso3code IN""" ++ inFragment ++
          fr"""
        )
        SELECT
            c.name AS country,
            SUM(gg.growth) AS total_growth
        FROM
            gdp_growth gg
            JOIN countries c ON gg.countryiso3code = c.countryiso3code
        GROUP BY
            gg.countryiso3code
        ORDER BY
            total_growth DESC
        LIMIT 3;
         """

      q.query[QueryResult].to[List].transact(xa).recoverWith {
        error =>
          println(error.toString)
          Concurrent[F].pure(List())
      }
    }
  }
}
