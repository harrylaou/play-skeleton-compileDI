package com.harrylaou.play

import zio.ZLayer

import com.harrylaou.play.application.results.AppError

package object application {

  type AppLayer[A] = ZLayer[Any, AppError, A]

}
