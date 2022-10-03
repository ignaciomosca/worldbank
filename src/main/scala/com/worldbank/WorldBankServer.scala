package com.worldbank

import cats.{FlatMap, Parallel}
import cats.effect.Async
import cats.effect.kernel.Concurrent
import cats.implicits._
import doobie.util.transactor.Transactor
import org.http4s.ember.client.EmberClientBuilder

object WorldBankServer {

  def process[F[_]: Async: Parallel](isIngestion: Boolean, xa: Transactor[F]) = if(isIngestion) { ingestion(xa) } else {
    query(xa).map(println)
  }

  def ingestion[F[_]: Async: Parallel](xa: Transactor[F]): F[Unit] = {
    EmberClientBuilder.default[F].build.use{ client =>
      val worldBank = WorldBankRepo.impl(xa)
      val ingestion = Ingestion.impl(client, worldBank)
      ingestion.ingestData
    }
  }

  def query[F[_]: Async: Concurrent](xa: Transactor[F]) = {
    val worldBank = WorldBankRepo.impl(xa)
    val query = Query.impl(worldBank)
    for {
      worldPopulation <- query.queryWorldPopulation()
      worldGDP <- query.queryWorldDP(worldPopulation)
    } yield {
      worldGDP
    }
  }
}
