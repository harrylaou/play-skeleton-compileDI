package com.harrylaou.play.application.results

import java.util.Objects

import cats.Semigroup
import cats.data.{NonEmptyChain, NonEmptyList}
import cats.implicits.*
import play.api.http.Writeable
import play.api.mvc.{Result, Results}

import io.circe.{DecodingFailure, Json, Printer}

/** Represents the system error.
  */
trait AppError {

  def status: Results.Status
  def message: String
  protected def maybeThrowable: Option[Throwable]

  def more: Option[NonEmptyChain[String]]

  implicit val writableOfCirce: Writeable[Json] =
    AppResults.writableOf_Json(play.api.mvc.Codec.utf_8, Printer.spaces2)

  def logMessage: String = Either.catchNonFatal(message.replaceAll("\n", " ")).getOrElse("")

  lazy val asJson: Json =
    Json.obj(
      "status"     -> Json.fromString(status.toString),
      "message"    -> Json.fromString(logMessage),
      "more"       -> more.fold(Json.Null)(m => Json.fromValues(m.toNonEmptyList.toList.map(Json.fromString))),
      "stacktrace" -> maybeThrowable.fold(Json.Null)(th => Json.fromString(th.getStackTrace.mkString("\n")))
    )

  lazy val asLoggableJson: Json = asJson.hcursor.downField("stacktrace").delete.top.getOrElse(Json.fromString("{}"))

  def toResult: Result = status(asLoggableJson)

  def throwable: Throwable = maybeThrowable.getOrElse(new RuntimeException(logMessage))

  def combine(other: AppError): AppError = AppError.appErrorSemigroup.combine(this, other)

  def withStatus(_status: Results.Status): AppError =
    AppError.AppErrorImpl(status = _status, message = logMessage, more = more, maybeThrowable = maybeThrowable)

  def cleanException: AppError  = AppError.AppErrorImpl(
    status = this.status,
    message = this.message,
    more = this.more,
    maybeThrowable = None
  )
  override def toString: String = asJson.spaces2

  override def equals(o: Any): Boolean = hashCode === o.hashCode

  override def hashCode: Int =
    Objects.hash(
      status,
      more
        .map(_.toNonEmptyList.:+(message).toList.toSet)
        .getOrElse(Set(message))
    )

}

object AppError {

  private[results] final case class AppErrorImpl(
    status: Results.Status,
    message: String,
    more: Option[NonEmptyChain[String]],
    protected val maybeThrowable: Option[Throwable]
  ) extends AppError

  val empty: AppError = AppError.fromS("empty")

  def fromTh(status: Results.Status)(throwable: Throwable): AppError =
    AppErrorImpl(
      status,
      throwable.getMessage,
      more = None,
      maybeThrowable = Option(throwable)
    )

  def fromTh(throwable: Throwable): AppError =
    fromTh(status = Results.InternalServerError)(throwable = throwable)

  def fromS(status: Results.Status)(message: String): AppError =
    AppErrorImpl(
      status = status,
      message = message,
      more = None,
      maybeThrowable = None
    )

  def modelNotFound(message: String): AppError = fromS(Results.Gone)(message)

  def unprocessableEntity(message: String): AppError = fromS(Results.UnprocessableEntity)(message)

  def fromS(message: String): AppError =
    AppErrorImpl(
      status = Results.InternalServerError,
      message = message,
      more = None,
      maybeThrowable = None
    )

  def fromDF(df: DecodingFailure, json: Json): AppError =
    AppErrorImpl(
      status = Results.UnprocessableEntity,
      message = df.getMessage() + json.noSpaces,
      more = None,
      maybeThrowable = Option(df)
    )

  def fromError[E](e: E): AppError = AppError.fromS(e.toString)

  def toResult(errors: NonEmptyList[AppError]): Result = {
    val newMessage = errors.toList
      .map(e => s"Status:${e.status.toString} - message: ${e.logMessage}")
      .mkString("\n")
    errors.head.status(newMessage)
  }

  def toDecodingFailure(error: AppError): DecodingFailure = DecodingFailure(error.logMessage, List.empty)

  implicit object appErrorSemigroup extends Semigroup[AppError] {

    override def combine(a: AppError, b: AppError): AppError = {

      val status: Results.Status = Results.Status(math.max(a.status.header.status, b.status.header.status))

      val more: Option[NonEmptyChain[String]] = (a.more, b.more) match {
        case (None, None)         => Option(NonEmptyChain(b.message))
        case (Some(m1), None)     => Option(m1.append(b.message))
        case (None, Some(m2))     => Option(m2.prepend(b.message))
        case (Some(m1), Some(m2)) => Option(m1.append(b.message).concat(m2))
      }
      AppErrorImpl(
        status = status,
        message = a.message,
        more = more,
        maybeThrowable = a.maybeThrowable.orElse(b.maybeThrowable)
      )
    }

  }

  val combine: (AppError, AppError) => AppError = appErrorSemigroup.combine

}
