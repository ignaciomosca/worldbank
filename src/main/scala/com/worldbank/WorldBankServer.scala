package com.worldbank

import cats.FlatMap
import cats.effect.Async
import doobie.util.transactor.Transactor
import org.http4s.ember.client.EmberClientBuilder

object WorldBankServer {

//  def process[F[_]: Async](param: String, xa: Transactor[F]) = param match {
//    case "ingestion" => ingestion(xa)
//    case "query" => query(xa)
//  }

  def ingestion[F[_]: Async](xa: Transactor[F]): F[Unit] = {
    EmberClientBuilder.default[F].build.use{ client =>
      val worldBank = WorldBankRepo.impl(xa)
      val ingestion = Ingestion.impl(client, worldBank)
      ingestion.ingestData
    }
  }

//  def query[F[_]: Async: FlatMap](xa: Transactor[F]) = {
//    val worldBank = WorldBankRepo.impl(xa)
//    val query = Query.impl(worldBank)
//    for {
//      worldGDP <- query.queryWorldDP()
//      worldPopulation <- query.queryWorldPopulation()
//    } yield worldGDP ++ worldPopulation
//  }
}
