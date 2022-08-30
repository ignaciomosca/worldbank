package com.worldbank

import cats.Applicative
import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder
import org.http4s.EntityEncoder
import org.http4s.circe.jsonEncoderOf

object QueryEntities {
  case class QueryResult(countryiso3code: String, growth: Double)
  case class QueryResults(populationGrowth: List[QueryResult], gdpGrowth: List[QueryResult])
  implicit val DBPopulationEncoder: Encoder[QueryResult] = deriveEncoder[QueryResult]
  implicit def DBPopulationListEntityEncoder[F[_]: Applicative]: EntityEncoder[F, List[QueryResult]] = jsonEncoderOf
  implicit val QueryResultsEncoder: Encoder[QueryResults] = deriveEncoder[QueryResults]
  implicit def QueryResultsEntityEncoder[F[_]: Applicative]: EntityEncoder[F, QueryResults] = jsonEncoderOf
}
