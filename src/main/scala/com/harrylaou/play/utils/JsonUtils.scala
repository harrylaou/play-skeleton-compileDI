package com.harrylaou.play.utils

import cats.implicits.*

import io.circe.*
import io.circe.parser.*

import com.harrylaou.play.application.logging.AppLogger

object JsonUtils {

  def toJson(jsonString: String)(implicit logger: AppLogger): Json =
    parse(jsonString).leftMap { df =>
      logger.error(s"Cannot parse string $jsonString", df)
      df
    }.toOption.get

  def toModel[M](json: Json)(implicit decoder: Decoder[M], logger: AppLogger): M =
    json
      .as[M]
      .leftMap { df =>
        logger.error(s"Cannot parse json ${json.noSpaces} ", df)
        df
      }
      .toOption
      .get

  def toModel[M](jsonString: String)(implicit decoder: Decoder[M], logger: AppLogger): M =
    toModel(toJson(jsonString))

}
