package application.results

import java.io.File

import application.logging._
import io.circe.syntax._
import io.circe.{Encoder, Json, Printer}
import play.api.http.{FileMimeTypes, Writeable}
import play.api.libs.circe.Circe
import play.api.mvc.{PlayBodyParsers, Result, Results}

import scala.concurrent.{ExecutionContext, Future}

/**
  *
  * The usual usage is returnAsync(m)
  *
  * If we don't want to return Ok , but let's say Created
  * we call it like :
  * returnAsync(m,status=Results.Created)
  *
  *
  * if we don't want to return the model as json we provide a customResponse
  * like
  *returnAsync(m,customResponse=Option(_=>Redirect("...")))
  *
  */
trait AppResults extends Circe with AppLogging {

  implicit val writableOfCirce: Writeable[Json] =
    writableOf_Json(play.api.mvc.Codec.utf_8, Printer.spaces2)

  def returnAsync[M](
    asyncResult: AsyncResultT[M],
    status: Results.Status = Results.Ok,
    customResponse: Option[M => Result] = None
  )(implicit encoder: Encoder[M], ec: ExecutionContext): Future[Result] =
    returnAsync(asyncResult.value, status, customResponse)

  private def returnAsync[M](
    asyncResult: AsyncResult[M],
    status: Results.Status,
    customResponse: Option[M => Result]
  )(implicit encoder: Encoder[M], ec: ExecutionContext): Future[Result] =
    asyncResult.map {
      case Left(error) =>
        logger.error(error)
        error.toResult
      case Right(m) =>
        customResponse.fold(status(m.asJson))(resp => resp(m))
    }

  def returnSync[M](
    syncResult: SyncResult[M],
    status: Results.Status = Results.Ok,
    customResponse: Option[M => Result] = None
  )(implicit encoder: Encoder[M]): Result = syncResult match {
    case Left(error) =>
      logger.error(error)
      error.toResult
    case Right(m) =>
      customResponse.fold(status(m.asJson))(resp => resp(m))
  }

  def returnFile(
    fileSync: SyncResult[File]
  )(implicit ec: ExecutionContext, fileMimeTypes: FileMimeTypes): Result =
    fileSync match {
      case Left(error) =>
        logger.error(error)
        error.toResult
      case Right(file) =>
        Results.Ok.sendFile(
          content = file
        )
    }

}

object AppResults extends AppResults {
  override def parse: PlayBodyParsers = ???
}
