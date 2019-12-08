package application

import play.api.{Application, ApplicationLoader, LoggerConfigurator}
import play.api.ApplicationLoader.Context

class AppLoader extends ApplicationLoader {

  override def load(context: Context): Application = {
    LoggerConfigurator(context.environment.classLoader).foreach {
      _.configure(context.environment)
    }

    val components: AppComponents = new AppComponents(context)

    components.application

  }
}
