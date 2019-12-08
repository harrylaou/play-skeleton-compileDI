package controllers

import play.api.mvc._
class HomeController()(implicit val controllerComponents: ControllerComponents) extends BaseController {

  def index: Action[AnyContent] = Action {
    Ok("It works")
  }


}
