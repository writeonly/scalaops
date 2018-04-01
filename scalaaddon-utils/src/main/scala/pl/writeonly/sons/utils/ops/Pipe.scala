package pl.writeonly.sons.utils.ops

object Pipe {
  implicit class Pipe[A](a: A) {
    def |>[B](f: A => B) = f(a)

    def ? : Boolean = isNotNull(a)

    def ??(b: => A): A = if (a) a else b
  }

  implicit def isNotNull[A](a: A): Boolean = Option(a).isDefined
}