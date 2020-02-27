package application

import org.mockito.scalatest.MockitoSugar
import play.api.ApplicationLoader.Context
import play.api.mvc.ControllerComponents
import play.api.test.StubControllerComponentsFactory

/**
  * Override/mock any componenents here
  */
class TestAppComponents(context: Context)
    extends AppComponents(context)
    with StubControllerComponentsFactory
    with MockitoSugar {

  override implicit val cc: ControllerComponents = stubControllerComponents()

}
