package com.worldbank

import com.worldbank.IngestionEntities.{CountryData, GDP, Population}

object DBEntities {
  case class DBPopulation(countryiso3code: String, value: Long, year: Int, country: String)
  def toDBPopulation(p: Population): DBPopulation = {
    DBPopulation(p.countryiso3code, p.value.getOrElse(0), p.date.toInt, p.country.value)
  }
  case class DBGDP(countryiso3code: String, value: Double, year: Int, country: String)
  def toDBGDP(p: GDP): DBGDP = {
    DBGDP(p.countryiso3code, p.value.getOrElse(0), p.date.toInt, p.country.value)
  }
  case class DBPopulationData(countryiso3code: String)
  case class DBCountryData(countryiso3code: String, name: String, capitalCity: String, latitude: String, longitude: String)
  def toDBCountryData(c: CountryData): DBCountryData = {
    DBCountryData(c.id, c.name, c.capitalCity, c.latitude, c.longitude)
  }
}
