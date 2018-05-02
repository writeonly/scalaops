package pl.writeonly.addons.future.library

import pl.writeonly.addons.future.api.Ops.{GetOrFailed, InSideOut, TransRecover}
import pl.writeonly.addons.future.api.{EC, Types1, Utils}
import pl.writeonly.addons.pipe.Pipe._

import scala.concurrent.Future
import scala.concurrent.Future.{failed, successful}

import pl.writeonly.addons.ops.FutureOps._

object OptFuture extends Types1 with Utils {

  override type Value[A] = Option[A]

  override def getOrFailed[A](v: ValueFuture[A])(implicit ec: EC): Future[A] =
    v match {
      case Some(f: Future[A]) => f
      case None               => new IllegalStateException() |> failed
    }

  override def inSideOut[A](
    v: ValueFuture[A]
  )(implicit ec: EC): FutureValue[A] =
    v match {
      case Some(f: Future[A]) => for (a <- f) yield Option(a)
      case None               => None |> successful
    }

  override def transRecover[A](
    v: Future[A]
  )(implicit ec: EC): FutureRecovered[A] =
    v.transformAndRecover((s: A) => Option(s), { case _ => None })

  //    value.transform({
  //      case Success(s) => Success(Option(s))
  //      case Failure(_) => Success(None)
  //    })

  implicit class OptFutureGetOrFailed[A](v: ValueFuture[A])
      extends GetOrFailed[A] {
    override def getOrFailed(implicit ec: EC): Future[A] =
      OptFuture.getOrFailed(v)(ec)
  }

  implicit class OptFutureInSideOut[A](v: ValueFuture[A])
      extends InSideOut[Value[A]] {
    override def inSideOut(implicit ec: EC): FutureValue[A] =
      OptFuture.inSideOut(v)(ec)
  }

  implicit class OptFutureTransRecover[A](v: Future[A])
      extends TransRecover[Value[A]] {
    override def transRecover(implicit ec: EC): FutureRecovered[A] =
      OptFuture.transRecover(v)(ec)
  }

}
