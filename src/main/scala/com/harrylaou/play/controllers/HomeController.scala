package com.harrylaou.play.controllers

import scala.concurrent.ExecutionContext
import play.api.mvc.*
import zio.ZIO

import com.harrylaou.play.application.AppLayer
import com.harrylaou.play.application.results.AppResults

class HomeController()(implicit val controllerComponents: ControllerComponents, layer: AppLayer[Any])
    extends BaseController
    with AppResults {

  def index: Action[AnyContent] = Action.async {

    implicit val ec: ExecutionContext = controllerComponents.executionContext

    returnZIO(ZIO.succeed("It works!"))
  }

}
