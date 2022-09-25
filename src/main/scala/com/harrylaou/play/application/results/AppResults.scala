package com.harrylaou.play.application.results

import java.io.File

import scala.concurrent.{ExecutionContext, Future}
import play.api.http.{FileMimeTypes, Writeable}
import play.api.libs.circe.Circe
import play.api.mvc.{PlayBodyParsers, Result, Results}
import zio.{Runtime, Unsafe, ZEnvironment, ZIO}

import com.typesafe.scalalogging.LazyLogging
import io.circe.syntax.*
import io.circe.{Encoder, Json, Printer}

import com.harrylaou.play.application.logging.*

/** The usual usage is returnAsync(m)
  *
  * If we don't want to return Ok , but let's say Created we call it like : returnAsync(m,status=Results.Created)
  *
  * if we don't want to return the model as json we provide a customResponse like
  * returnAsync(m,customResponse=Option(_=>Redirect("...")))
  */

trait AppResults extends Circe with LazyLogging {

  implicit val writableOfCirce: Writeable[Json] =
    writableOf_Json(play.api.mvc.Codec.utf_8, Printer.spaces2)

  def returnZIO[R, M](
    zioResult: ZIO[R, AppError, M],
    status: Results.Status = Results.Ok,
    customResponse: Option[M => Result] = None
  )(implicit encoder: Encoder[M], r: ZEnvironment[R], ec: ExecutionContext): Future[Result] =
    Unsafe.unsafe { implicit unsafe =>
      Runtime.default.unsafe.runToFuture(zioResult.provideEnvironment(r).either)
    }.future.map { maybeResult: Either[AppError, M] =>
      maybeResult match {

        case Left(error) =>
          logger.error(error)
          error.toResult
        case Right(m)    =>
          customResponse.fold(status(m.asJson))(resp => resp(m))
      }
    }.recover { case th: Throwable =>
      logger.error("This should never happern", th)
      Results.InternalServerError(th.getMessage)
    }
  def returnFile[R](
    zioFile: ZIO[R, AppError, File]
  )(implicit ec: ExecutionContext, fileMimeTypes: FileMimeTypes, r: ZEnvironment[R]): Future[Result] =
    Unsafe.unsafe { implicit unsafe =>
      Runtime.default.unsafe.runToFuture(zioFile.provideEnvironment(r).either)
    }.future.map { maybeResult: Either[AppError, File] =>
      maybeResult match {
        case Left(error) =>
          logger.error(error)
          error.toResult
        case Right(file) => Results.Ok.sendFile(content = file)
      }
    }.recover { case th: Throwable =>
      logger.error("This should never happern", th)
      Results.InternalServerError(th.getMessage)
    }

}
object AppResults extends AppResults {
  override def parse: PlayBodyParsers = ???
}
