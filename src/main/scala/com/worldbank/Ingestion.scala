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
  def impl[F[_]: Concurrent: Parallel](client: Client[F], repo: WorldBankRepo[F]): Ingestion[F] = new Ingestion[F] {
    val baseUri = uri"https://api.worldbank.org/v2/country/all/indicator"
    val countryUri = uri"https://api.worldbank.org/v2/country"

    val totalPopulationUri = baseUri / "SP.POP.TOTL"
    val gdpUri = baseUri / "NY.GDP.MKTP.CD"

    def ingestData: F[Unit] = List(getTotalPopulationData(), getGDPData(), getCountries()).parSequence_

    private def getTotalPagesForPopulationRequest() = {
      println("Pages for getTotalPagesForPopulationRequest")
      val request = Request[F](Method.GET, totalPopulationUri.withQueryParam("format", "json").withQueryParam("page", "1"))
      val result = client
        .expect[WorldBankPopulationData](request).map(_.pageStats.pages).map(n => 1 to n).map(_.toList)
        .recoverWith{ e=>
          println(s"Error getting Pages for getTotalPagesForPopulationRequest. Error ${e.toString}")
            Concurrent[F].pure(List())
        }
      result
    }

    def getCountries(page: Int = 1): F[List[Int]] = {
      println(s"Page Country $page")
      val request = Request[F](Method.GET, countryUri.withQueryParam("format", "json").withQueryParam("page", page))
      val result = client.expect[WorldBankCountriesData](request)
      result flatMap { worldCountryData =>
        if(worldCountryData.pageStats.page <= worldCountryData.pageStats.pages) {
          repo.saveCountryData(worldCountryData.data).flatMap(_ => getCountries(page + 1))
        } else {
          Concurrent[F].pure(List())
        }
      }
    }


    private def getTotalPagesForGDPRequest() = {
      val request = Request[F](Method.GET, gdpUri.withQueryParam("format", "json").withQueryParam("page", "1"))
      client
        .expect[WorldBankGDPData](request).map(_.pageStats.pages).map(n => 1 to n).map(_.toList)
        .recoverWith{ e=>
          Concurrent[F].pure(List())
        }
    }

    private def getTotalPopulationRequest(page: Int) = {
      println(s"Page $page")
      val request = Request[F](Method.GET, totalPopulationUri.withQueryParam("format", "json").withQueryParam("page", page.toString))
      client
        .expect[WorldBankPopulationData](request).flatMap(data => repo.savePopulationData(data.populationData))
        .recoverWith{
          case InvalidMessageBodyFailure(details, _) =>
            println(s"Failed getTotalPopulationRequest. Error $details")
            Concurrent[F].pure(0)
        }
    }

    private def getTotalGDPRequest(page: Int) = {
      val request = Request[F](Method.GET, gdpUri.withQueryParam("format", "json").withQueryParam("page", page.toString))
      client
        .expect[WorldBankGDPData](request).flatMap(data => repo.saveGDPData(data.populationData))
        .recoverWith{ e=>
          println(s"Failed getTotalGDPRequest. Error ${e.toString}")
        Concurrent[F].pure(0)
      }
    }

    //Returns all the data from the SP.POP.TOTL
    def getTotalPopulationData() = {
      println("Total population")
      getTotalPagesForPopulationRequest().flatMap {list => list.map{
          page => getTotalPopulationRequest(page)
        }.sequence
      }
    }

    //Returns all the repositories from an organization
    def getGDPData(): F[List[Int]] = {
      println("Total GDP data")
        getTotalPagesForGDPRequest().flatMap {list => list.map{
          page => getTotalGDPRequest(page)
        }.sequence
      }
    }

  }
}
