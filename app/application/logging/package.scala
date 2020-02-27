package application

import application.results.AppError
import play.api.{Logger => PlayLogger}
package object logging {
  type AppLogger = PlayLogger

  implicit class RichLogger(val logger: AppLogger) extends AnyVal {

    def error(msg: String, err: AppError): Unit = error(err, msg)

    def warn(msg: String, err: AppError): Unit = warn(err, msg)

    def error(err: AppError, msg: String = ""): Unit =
      logger.error(msg + " - " + err.logMessage, err.throwable)

    def warn(err: AppError, msg: String = ""): Unit =
      logger.warn(msg + " - " + err.logMessage, err.throwable)
  }

}
