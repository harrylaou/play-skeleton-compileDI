package commons

import play.api.http.{HeaderNames, HttpProtocol, MimeTypes, Status}
import play.api.test.{
  DefaultAwaitTimeout,
  EssentialActionCaller,
  FutureAwaits,
  PlayRunners,
  ResultExtractors,
  RouteInvokers,
  StubControllerComponentsFactory,
  Writeables
}

/**
  * Using play.api.test.Helpers as trait to mixin
  */
trait TestHelpers
    extends PlayRunners
    with HeaderNames
    with Status
    with MimeTypes
    with HttpProtocol
    with DefaultAwaitTimeout
    with ResultExtractors
    with Writeables
    with EssentialActionCaller
    with RouteInvokers
    with FutureAwaits
    with StubControllerComponentsFactory
