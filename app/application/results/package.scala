package application

import java.util.concurrent.CompletableFuture

import application.logging._
import cats.data.EitherT
import cats.implicits._
import cats.syntax.EitherSyntax
import cats.{Applicative, Functor, Monad}
import play.api.mvc.Results

import scala.compat.java8.FutureConverters._
import scala.concurrent.duration.{Duration, _}
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success, Try}
package object results extends EitherSyntax {
  type SyncResult[A] = Either[AppError, A]

  object SyncResult {
    def apply[A](a: A): SyncResult[A] = a.asRight[AppError]
  }

  type AsyncResult[A] = Future[SyncResult[A]]

  type AsyncResultT[A] = EitherT[Future, AppError, A]

  object AsyncResult {

    def apply[A](syncResult: SyncResult[A]): AsyncResult[A] = Future.successful(syncResult)

    def apply[A](a: A): AsyncResult[A] =
      Future.successful(SyncResult(a))

    def error[A](error: AppError): AsyncResult[A] =
      Future.successful(Left[AppError, A](error))
  }

  object AsyncResultT {

    def apply[A](a: A): AsyncResultT[A] = apply(AsyncResult(a))

    def apply[A](fs: AsyncResult[A]): AsyncResultT[A] = EitherT(fs)

    def apply[A](syncResult: SyncResult[A]): AsyncResultT[A] = apply(AsyncResult(syncResult))

    def unit: AsyncResultT[Unit] = apply(())

    def error[A](error: AppError): AsyncResultT[A] = apply(AsyncResult.error(error))
  }

  implicit class RichAsyncResultT[A](val asyncResultT: AsyncResultT[A]) extends AnyVal {

    def getWithDefault(a: A)(implicit logger: AppLogger): A = asyncResultT.value.await.toOptionWithLog.getOrElse(a)

    @SuppressWarnings(Array("org.wartremover.warts.OptionPartial"))
    def unsafeGet(implicit logger: AppLogger): A = asyncResultT.value.await.toOptionWithLog.get

    def cleanException(implicit ec: Functor[Future]): AsyncResultT[A] =
      asyncResultT.leftMap(_.cleanException)

    /**
      * to run a side-effect like logging and return the same asyncResult
      * is like `fut.andThen {case Success(x) => ....}`
      *
      */
    @SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
    def andThen[U](f: A => U)(implicit ec: Functor[Future]): AsyncResultT[A] =
      for {
        a <- asyncResultT
        _ = f(a)
      } yield a

    def andThenAsyncResultT[U](f: A => AsyncResultT[U])(implicit ec: Monad[Future]): AsyncResultT[A] =
      for {
        a <- asyncResultT
        _ <- f(a)
      } yield a

    def logError(implicit logger: AppLogger, ec: Functor[Future]): AsyncResultT[A] =
      asyncResultT.leftMap(error => {
        logger.error(error)
        error
      })

    def recoverWithDefault(
      default: A
    )(implicit logger: AppLogger, executionContext: ExecutionContext): AsyncResultT[A] =
      asyncResultT.recover {
        case error =>
          logger.error(error)
          default
      }

  }

  implicit class RichTry[A](val tryA: Try[A]) extends AnyVal {

    def liftSync(errStatus: Results.Status = Results.UnprocessableEntity): SyncResult[A] =
      tryA.toEither
        .leftMap((th: Throwable) => AppError.fromTh(errStatus)(th))

    def liftAsync: AsyncResultT[A] = tryA.toEither.liftAsync

    def toOptionWithLog(implicit logger: AppLogger): Option[A] = tryA match {
      case Success(ld) => Option(ld)
      case Failure(err) =>
        logger.error(s"Error when getting $tryA", err)
        None
    }

  }

  implicit class RichEitherThrowable[A](val eitherThrowableA: Either[Throwable, A]) extends AnyVal {

    def liftSync: SyncResult[A] = eitherThrowableA.leftMap(AppError.fromTh)

    def liftAsync: AsyncResultT[A] = AsyncResultT[A](liftSync)

    def toOptionWithLog(implicit logger: AppLogger): Option[A] = eitherThrowableA match {
      case Right(ld) => Option(ld)
      case Left(err) =>
        logger.error(s"Error when getting $eitherThrowableA", err)
        None
    }
  }

  implicit class RichBase[A](val a: A) extends AnyVal {

    def pureAsync: AsyncResultT[A] = AsyncResultT[A](a)
  }

  implicit class RichAsyncResult[A](val asyncResult: AsyncResult[A]) extends AnyVal {

    def wrapAsync: AsyncResultT[A] = AsyncResultT[A](asyncResult)
  }

  implicit class RichSyncResult[A](val syncResult: SyncResult[A]) extends AnyVal {

    def liftAsync: AsyncResultT[A] = AsyncResultT[A](syncResult)

    @SuppressWarnings(Array("org.wartremover.warts.Throw"))
    def unsafeGet(implicit logger: AppLogger): A = syncResult match {
      case Right(ld) => ld
      case Left(err) =>
        logger.error(err)
        throw err.throwable
    }

    def toOptionWithLog(implicit logger: AppLogger): Option[A] = syncResult match {
      case Right(ld) => Option(ld)
      case Left(err) =>
        logger.error(err)
        None
    }
  }

  implicit class RichOption[A](val maybeA: Option[A]) extends AnyVal {

    def liftSync(ifNone: AppError): SyncResult[A] = maybeA.toRight(ifNone)

    def liftAsync(ifNone: AppError): AsyncResultT[A] =
      AsyncResultT(liftSync(ifNone))

  }

  implicit class RichFutureOption[A](val foa: Future[Option[A]]) extends AnyVal {

    def wrapAsync(ifNone: AppError)(implicit ec: ExecutionContext): AsyncResultT[A] =
      EitherT.fromOptionF(foa, ifNone)
  }

  implicit class RichFuture[A](futureA: => Future[A]) {

    def await(implicit duration: Duration = 60.seconds): A =
      Await.result(futureA, duration)

    def liftAsync(implicit ec: ExecutionContext): AsyncResultT[A] = {
      val recovered: AsyncResult[A] =
        futureA.map[SyncResult[A]](a => Right(a)).recover { case th: Throwable => Left(AppError.fromTh(th)) }

      AsyncResultT(recovered)
    }

  }

  implicit class RichFutureList[A](val futureListA: Future[List[A]]) extends AnyVal {

    def liftAsync(implicit ec: ExecutionContext): AsyncResultT[List[A]] =
      AsyncResultT[List[A]](
        futureListA
          .map[SyncResult[List[A]]](seq => Right(seq))
          .recover { case th: Throwable => Left(AppError.fromTh(th)) }
      )

    def mapList[B](f: A => B)(implicit ec: ExecutionContext): Future[List[B]] =
      futureListA.map(_.map(f))

  }

  implicit class RichAppError(val error: AppError) extends AnyVal {

    def pureAsync[A]: AsyncResultT[A] = AsyncResultT.error(error)
  }

  def catchNonFatal[A](fa: => A): AsyncResultT[A] =
    AsyncResultT(Either.catchNonFatal(fa).leftMap(AppError.fromTh))

  @SuppressWarnings(Array("org.wartremover.warts.OptionPartial"))
  def cleanAppErrors[A](lista: List[SyncResult[A]])(implicit logger: AppLogger): List[A] = {
    val tup: (List[SyncResult[A]], List[SyncResult[A]]) = lista.partition(_.isLeft)
    val errors: List[SyncResult[A]]                     = tup._1
    val models: List[SyncResult[A]]                     = tup._2
    errors.foreach(
      e => logger.error(e.swap.toOption.get)
    )
    models.flatMap(syncResult => syncResult.toOptionWithLog.toList)
  }

  implicit class RichFutureTry[A](val futureTryA: Future[Try[A]]) extends AnyVal {

    def liftAsync(
      errStatus: Results.Status = Results.UnprocessableEntity
    )(implicit ec: ExecutionContext): AsyncResultT[A] =
      AsyncResultT[A](
        futureTryA.map(_.liftSync(errStatus)).recover {
          case ex: Exception => Left(AppError.fromS(Results.BadGateway)(ex.getMessage))
        }
      )
  }

  implicit class RichBool(val value: Boolean) extends AnyVal {

    def or[A <: AppError](err: => A)(implicit f: Applicative[Future]): AsyncResultT[Unit] =
      EitherT.cond(value, (), err)

  }

  implicit class RichJavaFuture[A](val future: CompletableFuture[A]) extends AnyVal {

    def toFuture: Future[A] = future.toScala

  }

  object ApplicativeSync extends Applicative[SyncResult] {

    @SuppressWarnings(Array("org.wartremover.contrib.warts.ExposedTuples"))
    override def product[A, B](fa: SyncResult[A], fb: SyncResult[B]): SyncResult[(A, B)] =
      (fa, fb) match {
        case (Right(a), Right(b))    => Right((a, b))
        case (Left(l), Right(_))     => Left(l)
        case (Right(_), Left(l))     => Left(l)
        case (Left(err), Left(err1)) => Left(err.combine(err1))
      }

    override def pure[A](a: A): SyncResult[A] = Right(a)

    override def ap[A, B](ff: SyncResult[A => B])(fa: SyncResult[A]): SyncResult[B] =
      fa.ap(ff)
  }
}
