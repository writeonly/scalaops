package pl.writeonly.scalaops.future.scalaz

import pl.writeonly.scalaops.future.api.Ops.{
  GetOrFailed,
  InSideOut,
  TransRecover
}
import pl.writeonly.scalaops.future.api.{EC, TypesRight, Utils}
import pl.writeonly.scalaops.ops.ToThrowableException.ToThrowable0Exception
import scalaz.Maybe
import scalaz.Maybe.{Empty, Just}

import scala.concurrent.Future

trait MaybeFuture extends TypesRight with Utils {
  override type Value[A] = Maybe[A]

  override def getOrFailed[A](v: FutureV[A])(implicit ec: EC): Future[A] =
    v match {
      case Just(f: Future[A]) => f
      case Empty()            => ToThrowable0Exception() |> Future.failed
    }

  override def inSideOut[A](v: FutureV[A])(implicit ec: EC): ValueF[A] =
    v match {
      case Just(f: Future[A]) => for (a <- f) yield Maybe.fromNullable(a)
      case a: Empty[A]        => Future.successful(a)
    }

  implicit class OptFutureGetOrFailed[A](v: FutureV[A]) extends GetOrFailed[A] {
    override def getOrFailed(implicit ec: EC): Future[A] =
      MaybeFuture.getOrFailed(v)(ec)
  }

  implicit class OptFutureInSideOut[A](v: FutureV[A])
      extends InSideOut[Value[A]] {
    override def inSideOut(implicit ec: EC): ValueF[A] =
      MaybeFuture.inSideOut(v)(ec)
  }

  implicit class OptFutureTransRecover[A](f: Future[A])
      extends TransRecover[A, Value[A]](f) {
    override def transformSuccess: A => Maybe[A] = Maybe.just

    override def recoverFailure: Throwable => Maybe[A] = _ => Maybe.empty
  }

}

object MaybeFuture extends MaybeFuture
