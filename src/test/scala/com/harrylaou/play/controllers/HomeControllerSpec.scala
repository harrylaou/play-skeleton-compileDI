package com.harrylaou.play.controllers

import scala.concurrent.Future
import play.api.mvc.{AnyContent, Request, Result}

import org.scalatest.{BeforeAndAfterAll, TestSuite}

import com.harrylaou.play.application.TestAppLoader.testComponents.*
import com.harrylaou.play.commons.AppSpec

class HomeControllerSpec extends AppSpec with BeforeAndAfterAll {
  this: TestSuite =>
  "The SEO controller" should {
    "return the sitemap" in {
      val req: Request[AnyContent] =
        createAnyRequest("/", method = GET, headers = List.empty)
      val result: Future[Result]   = homeController.index.apply(req)
      status(result) mustEqual OK
      contentAsString(result) mustEqual "It works!"

    }

  }
}
