package pl.writeonly.scalaops.ops.mono.api

trait PipeBothOps[A, M[_, _]] {
  type F[B] = (A, B) => A

  def pipeFold[B, C](b: M[C, B])(f: F[B]): A

  def pipeMap[B, C](b: M[C, B])(f: F[B]): A
}
