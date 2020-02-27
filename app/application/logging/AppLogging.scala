package application.logging

import play.api.{Logger => PlayLogger}

trait AppLogging {
  implicit val logger: AppLogger = PlayLogger(this.getClass)

}
