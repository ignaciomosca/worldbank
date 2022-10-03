package com.worldbank

import com.worldbank.QueryEntities.QueryResult

trait Query[F[_]]{
  def queryWorldPopulation(): F[List[QueryResult]]
  def queryWorldDP(countryCodes: List[QueryResult]): F[List[QueryResult]]
}

object Query {
  implicit def apply[F[_]](implicit ev: Query[F]): Query[F] = ev

  def impl[F[_]](repo: WorldBankRepo[F]): Query[F] = new Query[F]{
    def queryWorldPopulation(): F[List[QueryResult]] =
      repo.top10PopulationGrowth()

    def queryWorldDP(countryCodes: List[QueryResult]): F[List[QueryResult]] =
      repo.top3GDBGrowth(countryCodes.map(_.countryiso3code))

  }
}
