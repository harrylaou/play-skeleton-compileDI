package utils

import application.logging.AppLogging
import cats.implicits._
import io.circe._
import io.circe.parser._

@SuppressWarnings(Array("org.wartremover.warts.OptionPartial"))
object JsonUtils extends AppLogging {

  def toJson(jsonString: String): Json =
    parse(jsonString).leftMap { df =>
      logger.error(s"Cannot parse string $jsonString", df)
      df
    }.toOption.get

  def toModel[M](json: Json)(implicit decoder: Decoder[M]): M =
    json
      .as[M]
      .leftMap { df =>
        logger.error(s"Cannot parse json ${json.noSpaces} ", df)
        df
      }
      .toOption
      .get

  def toModel[M](jsonString: String)(implicit decoder: Decoder[M]): M =
    toModel(toJson(jsonString))

}
