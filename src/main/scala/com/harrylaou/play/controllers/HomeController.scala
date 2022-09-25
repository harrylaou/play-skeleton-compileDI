package com.harrylaou.play.controllers

import scala.concurrent.ExecutionContext
import play.api.mvc.*
import zio.{ZEnvironment, ZIO}

import com.harrylaou.play.application.results.AppResults

class HomeController()(implicit val controllerComponents: ControllerComponents) extends BaseController with AppResults {

  def index: Action[AnyContent] = Action.async {

    implicit val r: ZEnvironment[Any] = ZEnvironment.empty
    implicit val ec: ExecutionContext = controllerComponents.executionContext

    returnZIO(ZIO.succeed("It works!"))
  }

}
