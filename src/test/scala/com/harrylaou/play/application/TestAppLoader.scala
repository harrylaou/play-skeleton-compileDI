package com.harrylaou.play.application

import com.typesafe.config.ConfigFactory
import play.api.ApplicationLoader.Context
import play.api.inject.DefaultApplicationLifecycle
import play.api.{Application, Configuration, Environment, LoggerConfigurator}

/** Note : this is a a bit different than the application.AppLoader because the appComponents are needed, so it doesn't
  * extend the play.api.ApplicationLoader trait
  */
object TestAppLoader {

  /** Similar to play.api.ApplicationLoader.load
    */
  def load(context: Context): (AppComponents, Application) = {
    LoggerConfigurator(context.environment.classLoader).foreach {
      _.configure(context.environment)
    }
    val appComponents = new TestAppComponents(context)
    (appComponents, appComponents.application)
  }

  val environment: Environment     = Environment.simple()
  val configuration: Configuration = Configuration(ConfigFactory.load("application.test.conf"))

  val context: Context =
    Context(
      environment = environment,
      initialConfiguration = configuration,
      lifecycle = new DefaultApplicationLifecycle(),
      devContext = None
    )

  lazy val (testComponents: AppComponents, fakeApplication: Application) = TestAppLoader.load(context)

}
