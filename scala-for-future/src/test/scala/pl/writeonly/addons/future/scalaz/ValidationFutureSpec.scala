package pl.writeonly.addons.future.scalaz

import org.scalatest.EitherValues
import pl.writeonly.addons.future.RemoteService
import pl.writeonly.addons.future.RemoteService.{ClientException, FutureResult}
import pl.writeonly.addons.ops.ToThrowableException
import pl.writeonly.sons.specs.WhiteFutureSpec
import scalaz.{Failure, Success, Validation}

import scala.concurrent.Future

class ValidationFutureSpec
    extends WhiteFutureSpec
    with EitherValues
    with ValidationFuture {
  describe("A Validation") {
    describe("for Success with successful") {
      val v: Validation[String, FutureResult] =
        Validation.success(Future.successful(1))
      it("inSideOut") {
        for {
          i <- v.inSideOut
        } yield {
          i shouldBe Success(1)
        }
      }
      it("getOrFailed") {
        for {
          i <- v.getOrFailed
        } yield {
          i shouldBe 1
        }
      }
      it("getOrFailed and transRecover") {
        for {
          i <- v.getOrFailed.transRecover
        } yield {
          i shouldBe Success(1)
        }
      }
    }
    describe("for Validation with successful") {
      val v: Validation[String, FutureResult] =
        Validation.failure(RemoteService.InternalServerError)
      it("inSideOut") {
        for {
          i <- v.inSideOut
        } yield {
          i shouldBe Failure(RemoteService.InternalServerError)
        }
      }
      it("getOrFailed") {
        recoverToSucceededIf[ToThrowableException] {
          for {
            i <- v.getOrFailed
          } yield {
            i shouldBe 1
          }
        }
      }
      it("getOrFailed and transRecover") {
        for {
          i <- v.getOrFailed.transRecover
        } yield {
          i.toEither.left.value shouldBe a[ToThrowableException]
        }
      }
    }
    describe("transRecover") {
      it("for successful") {
        for {
          s <- RemoteService.successful1.transRecover
        } yield {
          s shouldBe Success(1)
        }
      }
      it("for failed") {
        for {
          f <- RemoteService.failed0InternalServerError.transRecover
        } yield {
          f shouldBe Failure(ClientException())
        }
      }
    }
  }
}
