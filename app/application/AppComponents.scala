package application

import config.AppConfiguration
import controllers.HomeController
import play.api.ApplicationLoader.Context
import play.api.BuiltInComponentsFromContext
import play.api.libs.ws.WSClient
import play.api.libs.ws.ahc.AhcWSComponents
import play.api.mvc.{ControllerComponents, EssentialFilter}
import play.filters.cors.{CORSConfig, CORSFilter}
import router.Routes

class AppComponents(context: Context) extends BuiltInComponentsFromContext(context) with AhcWSComponents {

  implicit lazy val ws: WSClient        = wsClient
  implicit val cc: ControllerComponents = controllerComponents

  lazy val appConfiguration: AppConfiguration = new AppConfiguration(configuration)

  lazy val homeController: HomeController = new HomeController

  lazy val router: Routes = new Routes(
    httpErrorHandler,
    homeController
  )

  val corsFilter: CORSFilter =
    CORSFilter.apply(CORSConfig.fromConfiguration(context.initialConfiguration))

  override val httpFilters: Seq[EssentialFilter] = Seq(corsFilter)

}
