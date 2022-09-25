package com.harrylaou.play.application

import play.api.ApplicationLoader.Context
import play.api.mvc.ControllerComponents
import play.api.test.StubControllerComponentsFactory

/** Override/mock any components here
  */
class TestAppComponents(context: Context) extends AppComponents(context) with StubControllerComponentsFactory {

  override implicit val cc: ControllerComponents = stubControllerComponents()

}
