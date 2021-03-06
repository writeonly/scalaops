package pl.writeonly.scalaops.monoid.api.present

trait ToThrowable {

  import ToThrowableException._

  protected def toThrowable[A](a: A): Throwable = a match {
    case f: Throwable => f
    case _            => ToThrowable1Exception(a)
  }

  protected def toThrowable[A]: Throwable = ToThrowable0Exception[A]()

  def throwThrowable[A](t: Throwable) = throw t
}
