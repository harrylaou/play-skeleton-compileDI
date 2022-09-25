package com.harrylaou.play.controllers

import play.api.mvc.*

class HomeController()(implicit val controllerComponents: ControllerComponents) extends BaseController {

  def index: Action[AnyContent] = Action {
    Ok("It works!")
  }

}
