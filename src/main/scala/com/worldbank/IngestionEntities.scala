package com.worldbank

import cats.Applicative
import cats.effect.Concurrent
import io.circe.generic.auto._
import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder, HCursor}
import org.http4s.circe._
import org.http4s.{EntityDecoder, EntityEncoder}


object IngestionEntities {
  final case class PageStats(page: Int, pages: Int, per_page: Int, total: Int, sourceid: String, sourcename: String, lastupdated: String)
  final case class CountryPageStats(page: Int, pages: Int, per_page: Int, total: Int)
  final case class Error(reason: String, statusCode: Int)
  final case class Indicator(id: String, value: String)
  final case class Country(id: String, value: String)

  final case class CountryData(id: String, name: String, capitalCity: String, latitude: String, longitude: String)


  final case class Population(indicator: Indicator, country: Country, countryiso3code: String, date: String, value: Option[Long], unit: String, obs_status:String, decimal: Int)
  final case class GDP(indicator: Indicator, country: Country, countryiso3code: String, date: String, value: Option[Double], unit: String, obs_status:String, decimal: Int)
  final case class WorldBankPopulationData(pageStats: PageStats, populationData: List[Population])
  final case class WorldBankGDPData(pageStats: PageStats, populationData: List[GDP])
  final case class RealWorldBankData(body: List[WorldBankPopulationData])
  final case class WorldBankCountriesData(pageStats: CountryPageStats, data: List[CountryData])
  final case class WorldBankException(message: String, status: Int) extends RuntimeException

  // Encoders & Decoders
  implicit val PageStatsDecoder: Decoder[PageStats]                                                        = deriveDecoder[PageStats]
  implicit val PageStatsEncoder: Encoder[PageStats]                                                        = deriveEncoder[PageStats]
  implicit def PageStatsEntityDecoder[F[_]: Concurrent]: EntityDecoder[F, PageStats]                             = jsonOf
  implicit val ErrorEncoder: Encoder[Error]                                   = deriveEncoder[Error]
  implicit def WorldBankExceptionEntityEncoder[F[_]: Applicative]: EntityEncoder[F, WorldBankException] = jsonEncoderOf
  implicit val WorldBankExceptionEncoder: Encoder[WorldBankException]                                   = deriveEncoder[WorldBankException]
  implicit val WorldBankExceptionDecoder: Decoder[WorldBankException]                                   = deriveDecoder[WorldBankException]
  implicit def ErrorEntityEncoder[F[_]: Applicative]: EntityEncoder[F, Error] = jsonEncoderOf
  implicit val IndicatorEncoder: Encoder[Indicator]                                   = deriveEncoder[Indicator]
  implicit val IndicatorDecoder: Decoder[Indicator]                                                        = deriveDecoder[Indicator]
  implicit def IndicatorEntityEncoder[F[_]: Applicative]: EntityEncoder[F, Indicator] = jsonEncoderOf
  implicit val CountryEncoder: Encoder[Country]                                   = deriveEncoder[Country]
  implicit val CountryDecoder: Decoder[Country]                                                        = deriveDecoder[Country]
  implicit def CountryEntityEncoder[F[_]: Applicative]: EntityEncoder[F, Country] = jsonEncoderOf
  implicit val PopulationDecoder: Decoder[Population]                                                        = deriveDecoder[Population]
  implicit val PopulationEncoder: Encoder[Population]                                                        = deriveEncoder[Population]

  implicit def PopulationListEntityEncoder[F[_]: Applicative]: EntityEncoder[F, List[Population]] = jsonEncoderOf
  implicit def PopulationListEntityDecoder[F[_]: Concurrent]: EntityDecoder[F, List[Population]] =
    jsonOf[F, List[Population]]
  implicit val PopulationDataEncoder: Encoder[WorldBankPopulationData]                                                        = deriveEncoder[WorldBankPopulationData]
  implicit def WorldBankDataEntityDecoder[F[_]: Concurrent]: EntityDecoder[F, WorldBankPopulationData]                             = jsonOf
  implicit val RealWorldBankDataDecoder: Decoder[RealWorldBankData]                                                        = deriveDecoder[RealWorldBankData]
  implicit val RealWorldBankDataEncoder: Encoder[RealWorldBankData]                                                        = deriveEncoder[RealWorldBankData]
  implicit def RealWorldBankDataEntityDecoder[F[_]: Concurrent]: EntityDecoder[F, RealWorldBankData]                             = jsonOf
  implicit def RealWorldBankCountryDataEntityDecoder[F[_]: Concurrent]: EntityDecoder[F, WorldBankCountriesData]                             = jsonOf
  implicit val ListWorldBankDataDecoder: Decoder[WorldBankPopulationData] = new Decoder[WorldBankPopulationData] {
    final def apply(c: HCursor): Decoder.Result[WorldBankPopulationData] =
      for {
        pageStats <- c.downN(0).as[PageStats]
        populationList <- c.downN(1).as[List[Population]]
      } yield {
        WorldBankPopulationData(pageStats, populationList)
      }
  }

  implicit val worldBankCountriesDataEncoder: Encoder[WorldBankCountriesData] = deriveEncoder[WorldBankCountriesData]
  implicit val ListWorldBankCountriesDataDecoder: Decoder[WorldBankCountriesData] = new Decoder[WorldBankCountriesData] {
    final def apply(c: HCursor): Decoder.Result[WorldBankCountriesData] =
      for {
        pageStats <- c.downN(0).as[CountryPageStats]
        countryList <- c.downN(1).as[List[CountryData]]
      } yield {
        WorldBankCountriesData(pageStats, countryList)
      }
  }




  implicit val GDPEncoder: Encoder[GDP]                                                        = deriveEncoder[GDP]
  implicit def GDPDataEntityDecoder[F[_]: Concurrent]: EntityDecoder[F, WorldBankGDPData]                             = jsonOf
  implicit val WorldBankGDPDataDecoder: Decoder[WorldBankGDPData] = new Decoder[WorldBankGDPData] {
    final def apply(c: HCursor): Decoder.Result[WorldBankGDPData] =
      for {
        pageStats <- c.downN(0).as[PageStats]
        gdpList <- c.downN(1).as[List[GDP]]
      } yield {
        WorldBankGDPData(pageStats, gdpList)
      }
  }

  implicit val ListWorldBankDataEncoder: Encoder[List[WorldBankPopulationData]]                                                        = deriveEncoder[List[WorldBankPopulationData]]
  implicit def ListWorldBankDataEntityDecoder[F[_]: Concurrent]: EntityDecoder[F, List[WorldBankPopulationData]]                             = jsonOf

}