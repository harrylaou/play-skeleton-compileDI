package application

import controllers.HomeController
import play.api.ApplicationLoader.Context
import play.api.BuiltInComponentsFromContext
import play.api.mvc.{ControllerComponents, EssentialFilter}
import play.filters.cors.{CORSConfig, CORSFilter}
import router.Routes

class AppComponents(context: Context) extends BuiltInComponentsFromContext(context) {

  implicit val contrComponents: ControllerComponents = controllerComponents

  private lazy val homeController: HomeController = new HomeController

  lazy val router: Routes = new Routes(
    httpErrorHandler,
    homeController
  )

  val corsFilter: CORSFilter =
    CORSFilter.apply(CORSConfig.fromConfiguration(context.initialConfiguration))

  override val httpFilters: Seq[EssentialFilter] = Seq(corsFilter)

}
