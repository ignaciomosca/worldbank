package com.worldbank

import cats.Parallel
import cats.effect.Concurrent
import cats.implicits._
import com.worldbank.IngestionEntities.{WorldBankCountriesData, WorldBankGDPData, WorldBankPopulationData}
import org.http4s._
import org.http4s.client.Client
import org.http4s.implicits._

trait Ingestion[F[_]] {
  def ingestData: F[Unit]
}

object Ingestion {
  def impl[F[_] : Concurrent : Parallel](client: Client[F], repo: WorldBankRepo[F]): Ingestion[F] = new Ingestion[F] {
    val baseUri = uri"https://api.worldbank.org/v2/country/all/indicator"
    val countryUri = uri"https://api.worldbank.org/v2/country"

    val totalPopulationUri = baseUri / "SP.POP.TOTL"
    val gdpUri = baseUri / "NY.GDP.MKTP.CD"

    def ingestData: F[Unit] = List(getTotalPopulationData(), getGDPData(), getCountries()).parSequence_

    def getTotalPopulationData(page: Int = 1): F[List[Int]] = {
      println("Total population")
      val request = Request[F](Method.GET, totalPopulationUri.withQueryParam("format", "json").withQueryParam("page", page))
      val result = client.expect[WorldBankPopulationData](request)
      result flatMap { populationData =>
        if (populationData.pageStats.page <= populationData.pageStats.pages) {
          repo.savePopulationData(populationData.populationData).flatMap(_ => getTotalPopulationData(page + 1))
        } else {
          Concurrent[F].pure(List())
        }
      }
    }


    def getGDPData(page: Int = 1): F[List[Int]] = {
      println("Total GDP")
      val request = Request[F](Method.GET, gdpUri.withQueryParam("format", "json").withQueryParam("page", page))
      val result = client.expect[WorldBankGDPData](request)
      result flatMap { gdpData =>
        if (gdpData.pageStats.page <= gdpData.pageStats.pages) {
          repo.saveGDPData(gdpData.populationData).flatMap(_ => getGDPData(page + 1))
        } else {
          Concurrent[F].pure(List())
        }
      }
    }

    def getCountries(page: Int = 1): F[List[Int]] = {
      println(s"Page Country $page")
      val request = Request[F](Method.GET, countryUri.withQueryParam("format", "json").withQueryParam("page", page))
      val result = client.expect[WorldBankCountriesData](request)
      result flatMap { worldCountryData =>
        if (worldCountryData.pageStats.page <= worldCountryData.pageStats.pages) {
          repo.saveCountryData(worldCountryData.data.filterNot(_.capitalCity.isEmpty)).flatMap(_ => getCountries(page + 1))
        } else {
          Concurrent[F].pure(List())
        }
      }
    }
  }
}
