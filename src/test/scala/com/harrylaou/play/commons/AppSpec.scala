package com.harrylaou.play.commons

import scala.concurrent.Future
import akka.util.Timeout
import play.api.Application
import play.api.mvc.{AnyContent, Request, Result}
import play.api.test.FakeRequest

import com.typesafe.scalalogging.{LazyLogging, Logger}
import io.circe.parser.parse as jsonparse
import io.circe.{Decoder, Json}
import org.scalatest.TestSuite
import org.scalatestplus.play.{BaseOneAppPerSuite, FakeApplicationFactory, PlaySpec}

import com.harrylaou.play.application.TestAppLoader
import com.harrylaou.play.utils.JsonUtils

/** Common spec with fakeApplication
  */
trait AppSpec extends PlaySpec with BaseOneAppPerSuite with FakeApplicationFactory with TestHelpers with LazyLogging {
  this: TestSuite =>

  implicit lazy val log: Logger = logger

  type AppHeader = (String, String)
  lazy val fakeApplication: Application = TestAppLoader.fakeApplication

  protected def createRequestWithBody(
    endpoint: String,
    method: String,
    jsonString: String,
    headers: Seq[AppHeader]
  ): Request[Json] =
    FakeRequest(method, endpoint)
      .withHeaders(headers*)
      .withBody[Json](jsonparse(jsonString).getOrElse(Json.Null))

  protected def createRequest(endpoint: String, method: String, headers: Seq[AppHeader]): Request[Json] =
    FakeRequest(method, endpoint)
      .withBody[Json](body = Json.Null)
      .withHeaders(headers*)

  protected def createAnyRequest(
    endpoint: String,
    method: String,
    headers: List[AppHeader]
  ): Request[AnyContent] =
    FakeRequest(method, endpoint)
      .withHeaders(headers*)

  def contentAsCirceJson(result: Future[Result])(implicit timeout: Timeout): Json = {
    val resultAsString = contentAsString(result)(timeout)
    JsonUtils.toJson(resultAsString)
  }

  def contentAsModel[M](result: Future[Result])(implicit timeout: Timeout, decoder: Decoder[M]): M = {
    val resultAsString = contentAsString(result)(timeout)
    val json: Json     = JsonUtils.toJson(resultAsString)
    JsonUtils.toModel[M](json)
  }
}
