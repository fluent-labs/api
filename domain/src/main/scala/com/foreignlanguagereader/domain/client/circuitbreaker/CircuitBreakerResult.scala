package com.foreignlanguagereader.domain.client.circuitbreaker

import cats.Monad

sealed trait CircuitBreakerResult[T]
case class CircuitBreakerAttempt[T](result: T) extends CircuitBreakerResult[T]
case class CircuitBreakerFailedAttempt[T](e: Throwable)
    extends CircuitBreakerResult[T]
case class CircuitBreakerNonAttempt[T]() extends CircuitBreakerResult[T]

object CircuitBreakerResult {
  implicit def monad: Monad[CircuitBreakerResult] =
    new Monad[CircuitBreakerResult] {
      override def map[A, B](
          fa: CircuitBreakerResult[A]
      )(f: A => B): CircuitBreakerResult[B] =
        fa match {
          case CircuitBreakerNonAttempt()     => CircuitBreakerNonAttempt()
          case CircuitBreakerFailedAttempt(e) => CircuitBreakerFailedAttempt(e)
          case CircuitBreakerAttempt(result)  => CircuitBreakerAttempt(f(result))
        }

      override def pure[A](x: A): CircuitBreakerResult[A] =
        CircuitBreakerAttempt(x)

      override def ap[A, B](ff: CircuitBreakerResult[A => B])(
          fa: CircuitBreakerResult[A]
      ): CircuitBreakerResult[B] = {
        fa match {
          case CircuitBreakerFailedAttempt(e) => CircuitBreakerFailedAttempt(e)
          case CircuitBreakerNonAttempt()     => CircuitBreakerNonAttempt()
          case CircuitBreakerAttempt(r) =>
            ff match {
              case CircuitBreakerAttempt(f)   => CircuitBreakerAttempt(f(r))
              case CircuitBreakerNonAttempt() => CircuitBreakerNonAttempt()
              case CircuitBreakerFailedAttempt(e) =>
                CircuitBreakerFailedAttempt(e)
            }
        }
      }

      override def flatMap[A, B](fa: CircuitBreakerResult[A])(
          f: A => CircuitBreakerResult[B]
      ): CircuitBreakerResult[B] =
        fa match {
          case CircuitBreakerAttempt(a)       => f(a)
          case CircuitBreakerNonAttempt()     => CircuitBreakerNonAttempt()
          case CircuitBreakerFailedAttempt(e) => CircuitBreakerFailedAttempt(e)
        }

      override def tailRecM[A, B](a: A)(
          f: A => CircuitBreakerResult[Either[A, B]]
      ): CircuitBreakerResult[B] =
        f(a) match {
          case CircuitBreakerAttempt(b) =>
            b match {
              case Left(c) =>
                CircuitBreakerFailedAttempt(
                  new IllegalArgumentException(
                    s"Function returned wrong type: $c"
                  )
                )
              case Right(c) => CircuitBreakerAttempt(c)
            }
          case CircuitBreakerNonAttempt()     => CircuitBreakerNonAttempt()
          case CircuitBreakerFailedAttempt(e) => CircuitBreakerFailedAttempt(e)
        }
    }
}
