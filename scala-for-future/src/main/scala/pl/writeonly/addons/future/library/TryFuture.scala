package pl.writeonly.addons.future.library

import pl.writeonly.addons.future.api.Ops.{GetOrFailed, InSideOut, TransRecover}
import pl.writeonly.addons.future.api.{EC, Types1, Utils}
import pl.writeonly.addons.pipe.Pipe._

import scala.concurrent.Future
import scala.concurrent.Future.{failed, successful}
import scala.util.{Failure, Success, Try}
import pl.writeonly.addons.ops.FutureOps._

object TryFuture extends Types1 with Utils {

  override type Value[A] = Try[A]

  def getOrFailed[A](v: ValueFuture[A])(implicit ec: EC): Future[A] =
    v match {
      case Success(f: Future[A]) => f
      case Failure(f: Throwable) => f |> failed
    }

  def inSideOut[A](v: ValueFuture[A])(implicit ec: EC): FutureValue[A] =
    v match {
      case Success(f: Future[A]) => for (a <- f) yield Success(a)
      case a: Failure[A]         => a |> successful
    }

  override def transRecover[A](
    v: Future[A]
  )(implicit ec: EC): FutureRecovered[A] =
    v.transformAndRecover((s: A) => Success(s), { case t => Failure(t) })

  //    value.transformWith(Future.successful)

  implicit class TryFutureGetOrFailed[A](value: ValueFuture[A])
      extends GetOrFailed[A] {
    override def getOrFailed(implicit ec: EC): Future[A] =
      TryFuture.getOrFailed(value)(ec)
  }

  implicit class TryFutureInSideOut[A](value: ValueFuture[A])
      extends InSideOut[Value[A]] {
    override def inSideOut(implicit ec: EC): FutureValue[A] =
      TryFuture.inSideOut(value)(ec)
  }

  implicit class TryFutureTransRecover[A](value: Future[A])
      extends TransRecover[Recovered[A]] {

    override def transRecover(implicit ec: EC): FutureRecovered[A] =
      TryFuture.transRecover(value)(ec)

  }

}
