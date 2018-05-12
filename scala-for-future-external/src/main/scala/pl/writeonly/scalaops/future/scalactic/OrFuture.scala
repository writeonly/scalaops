package pl.writeonly.scalaops.future.scalactic

import org.scalactic.{Bad, Good, Or}
import pl.writeonly.scalaops.future.api.Ops.{
  GetOrFailed,
  InSideOut,
  TransRecover
}
import pl.writeonly.scalaops.future.api.{EC, TypesBoth, Utils}

import scala.concurrent.Future

trait OrFuture extends TypesBoth with Utils {

  override type Value[A, B] = B Or A

  override def getOrFailed[A, B](v: FutureV[A, B])(implicit ec: EC): Future[B] =
    v match {
      case Good(f: Future[B]) => f
      case Bad(f: A)          => f |> toThrowable[A] |> Future.failed
    }

  override def inSideOut[A, B](
    v: FutureV[A, B]
  )(implicit ec: EC): ValueF[A, B] =
    v match {
      case Good(f: Future[B]) => for (a <- f) yield Good(a)
      case a @ Bad(_)         => a |> Future.successful
    }

  //  override def recover[A](v: Future[A])(implicit ec: EC): Recovered[A] = ???

  def transRecover[A](v: Future[A])(implicit ec: EC): RecoveredF[A] =
    v.transformAndRecover(s => Good(s), Bad.apply)

  implicit class OrFutureInSideOut[B, A](v: FutureV[A, B])
      extends InSideOut[Value[A, B]] {
    override def inSideOut(implicit ec: EC): ValueF[A, B] =
      OrFuture.inSideOut(v)(ec)
  }

  implicit class OrFutureGetOrFailed[A, B](v: FutureV[A, B])
      extends GetOrFailed[B] {
    override def getOrFailed(implicit ec: EC): Future[B] =
      OrFuture.getOrFailed(v)(ec)
  }

  implicit class OrFutureTransRecover[A](f: Future[A])
      extends TransRecover[A, Recovered[A]](f) {
    override def transformSuccess: A => Recovered[A] = Good(_)

    override def recoverFailure: Throwable => Recovered[A] = Bad(_)
  }

}

object OrFuture extends OrFuture
