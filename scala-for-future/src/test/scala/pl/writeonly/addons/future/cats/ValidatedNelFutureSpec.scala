package pl.writeonly.addons.future.cats

import cats.data.Validated.{Invalid, Valid}
import cats.data.{NonEmptyList, Validated, ValidatedNel}
import cats.implicits._
import org.scalatest.EitherValues
import pl.writeonly.addons.future.RemoteService
import pl.writeonly.addons.future.RemoteService.{CaseException, FutureResult}
import pl.writeonly.addons.future.RemoteTuple.RemoteTuple3
import pl.writeonly.addons.future.cats.ValidatedNelFuture._
import pl.writeonly.sons.specs.WhiteFutureSpec

import scala.concurrent.Future

class ValidatedNelFutureSpec extends WhiteFutureSpec with EitherValues {
  describe("A ValidatedNel") {
    describe("for Valid with successful") {
      val v: ValidatedNel[String, FutureResult] =
        Validated.validNel(Future.successful(1))
      it("inSideOut") {
        for {
          i <- v.inSideOut
        } yield {
          i shouldBe Valid(1)
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
          i shouldBe Valid(1)
        }
      }
    }
    describe("for Invalid ") {
      val v: ValidatedNel[String, FutureResult] =
        Validated.invalidNel(CaseException().message)
      it("inSideOut") {
        for {
          i <- v.inSideOut
        } yield {
          i shouldBe Invalid(NonEmptyList.one(CaseException().message))
        }
      }
      it("getOrFailed") {
        recoverToSucceededIf[IllegalStateException] {
          for {
            i <- v.getOrFailed
          } yield {
            i shouldBe NonEmptyList.one(CaseException().message)
          }
        }
      }
      it("getOrFailed and transRecover") {
        for {
          i <- v.getOrFailed.transRecover
        } yield {
          i.toEither.left.value shouldBe a[NonEmptyList[Throwable]]
          i.toEither.left.value should have size 1
          i.toEither.left.value.head shouldBe a[IllegalStateException]
        }
      }
    }
    describe("for double Invalid ") {
      val v: ValidatedNel[String, FutureResult] = Validated.Invalid(
        NonEmptyList.of(RemoteService.NotImplemented, RemoteService.BadGateway)
      )
      it("inSideOut") {
        for {
          i <- v.inSideOut
        } yield {
          i shouldBe Invalid(
            NonEmptyList
              .of(RemoteService.NotImplemented, RemoteService.BadGateway)
          )
        }
      }
      it("getOrFailed") {
        recoverToSucceededIf[IllegalStateException] {
          for {
            i <- v.getOrFailed
          } yield {
            i shouldBe NonEmptyList.of(
              RemoteService.NotImplemented,
              RemoteService.BadGateway
            )
          }
        }
      }
      it("getOrFailed and transRecover") {
        for {
          i <- v.getOrFailed.transRecover
        } yield {
          i.toEither.left.value shouldBe a[NonEmptyList[Throwable]]
          i.toEither.left.value should have size 1
          i.toEither.left.value.head shouldBe a[IllegalStateException]
        }
      }

    }

    describe("transRecover") {
      it("for successful") {
        for {
          s <- RemoteService.successful1.transRecover
        } yield {
          s shouldBe Valid(1)
        }
      }
      it("for failed") {
        for {
          f <- RemoteService.failed0InternalServerError.transRecover
        } yield {
          f shouldBe Invalid(NonEmptyList.one(CaseException()))
        }
      }
      it("for successful and failed") {
        for {
          s <- RemoteService.successful1.transRecover
          f1 <- RemoteService.failed1NotImplemented.transRecover
          f2 <- RemoteService.failed2BadGateway.transRecover
          p = (s, f1, f2).mapN(RemoteTuple3[Int])
        } yield {
          s shouldBe Valid(1)
          f1 shouldBe Invalid(
            NonEmptyList.one(CaseException(RemoteService.NotImplemented))
          )
          f2 shouldBe Invalid(
            NonEmptyList.one(CaseException(RemoteService.BadGateway))
          )
          p shouldBe Invalid(
            NonEmptyList.of(
              CaseException(RemoteService.NotImplemented),
              CaseException(RemoteService.BadGateway)
            )
          )
        }
      }
    }
  }

}
