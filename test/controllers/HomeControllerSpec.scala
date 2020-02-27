package controllers

import application.TestAppLoader
import commons.AppSpec
import org.scalatest.{BeforeAndAfterAll, TestSuite}
import play.api.mvc.{AnyContent, Request, Result}

import scala.concurrent.Future
import TestAppLoader.testComponents._

@SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
class HomeControllerSpec extends AppSpec with BeforeAndAfterAll {
  this: TestSuite =>
  "The SEO controller" should {
    "return the sitemap" in {
      val req: Request[AnyContent] =
        createAnyRequest("/", method = GET, headers = List.empty)
      val result: Future[Result] = homeController.index.apply(req)
      status(result) mustEqual OK
      contentAsString(result) mustEqual ("It works!")

    }

  }
}
