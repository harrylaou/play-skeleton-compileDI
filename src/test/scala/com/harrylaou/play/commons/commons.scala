package com.harrylaou.play

import zio.{Runtime, Unsafe, ZIO}

import com.harrylaou.play.application.AppLayer
import com.harrylaou.play.application.results.AppError

package object commons {

  implicit class RichZIO[R, A](zio: ZIO[R, AppError, A]) {
    def unsafeGet(implicit layer: AppLayer[R]): A = Unsafe.unsafe { implicit unsafe =>
      Runtime.default.unsafe.run(zio.provideLayer(layer)).getOrThrowFiberFailure()
    }
  }

}
