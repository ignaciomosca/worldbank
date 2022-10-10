package com.worldbank

import cats.effect.IO
import cats.implicits._
import pureconfig.generic.auto
import pureconfig.generic.auto.exportReader
import pureconfig.ConfigSource
import pureconfig.error.ConfigReaderException

case class ServerConfig(username: String, pass: String, url: String)
object ServerConfig {
  def load(): IO[ServerConfig] = IO
    .fromEither(
    ConfigSource
      .default
      .load[ServerConfig]
      .leftMap(ConfigReaderException.apply))

}