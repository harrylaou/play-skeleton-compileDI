package com.harrylaou.play.config

import play.api.Configuration

import pureconfig.*

class AppConfiguration(configuration: Configuration) {
  val config: ConfigObjectSource = ConfigSource.fromConfig(configuration.underlying)

  /** Please note that if you want to have the system fail if configuration is not available, use `loadOrThrow` . If you
    * want the system to "recover", use `load` that returns a `SyncResult`
    */

//  val mongo: DummyConfiguration = config
//    .at("dummy")
//    .loadOrThrow[DummyConfiguration]

}
