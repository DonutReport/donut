package io.magentys

import scala.util.{Failure, Success, Try}

package object donut {
  implicit class TryExtension[A](t: Try[A]) {
    def toEither[E](f: Throwable => E): Either[E, A] = {
      t match {
        case Success(s) => Right(s)
        case Failure(e) => Left(f(e))
      }
    }
  }

  def sequenceEither[E, R](s: List[Either[E, R]]): Either[List[E], List[R]] = {
    val errors = s.filter(_.isLeft).map(e => e.left.get)
    if (errors.nonEmpty) Left(errors) else Right(s.map(e => e.right.get))
  }
}
