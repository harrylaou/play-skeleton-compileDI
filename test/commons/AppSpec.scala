package commons

import akka.util.Timeout
import application.TestAppLoader
import application.logging._
import io.circe.parser.{parse => jsonparse}
import io.circe.{Decoder, Json}
import org.mockito.scalatest.MockitoSugar
import org.scalatest.TestSuite
import org.scalatestplus.play.{BaseOneAppPerSuite, FakeApplicationFactory, PlaySpec}
import play.api.Application
import play.api.mvc.{AnyContent, Request, Result}
import play.api.test.FakeRequest
import utils.JsonUtils

import scala.concurrent.Future

/**
  * Common spec with fakeAplication
  */
trait AppSpec
    extends PlaySpec
    with BaseOneAppPerSuite
    with FakeApplicationFactory
    with AppLogging
    with MockitoSugar
    with TestHelpers {
  this: TestSuite =>

  type AppHeader = (String, String)
  lazy val fakeApplication: Application = TestAppLoader.fakeApplication

  protected def createRequestWithBody(
    endpoint: String,
    method: String,
    jsonString: String,
    headers: Seq[AppHeader]
  ): Request[Json] =
    FakeRequest(method, endpoint)
      .withHeaders(headers: _*)
      .withBody[Json](jsonparse(jsonString).getOrElse(Json.Null))

  protected def createRequest(endpoint: String, method: String, headers: Seq[AppHeader]): Request[Json] =
    FakeRequest(method, endpoint)
      .withBody[Json](body = Json.Null)
      .withHeaders(headers: _*)

  protected def createAnyRequest(
    endpoint: String,
    method: String,
    headers: List[AppHeader]
  ): Request[AnyContent] =
    FakeRequest(method, endpoint)
      .withHeaders(headers: _*)

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
