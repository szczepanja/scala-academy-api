package com.adform.scalaacademy.test

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.adform.scalaacademy.http.ErrorOut
import com.adform.scalaacademy.infrastructure.JsonSupport._
import io.circe.{Decoder, parser}

import scala.concurrent.duration.DurationInt
import scala.reflect.ClassTag
import scala.util.Right

trait TestSupport {

  implicit class EitherSupport[L, R](e: Either[L, R]) {
    def get: R = {
      e match {
        case Right(b) => b
        case Left(l)  => throw new NoSuchElementException(s"Either.right.get on Right, $l")
      }
    }

    def getLeft: L = {
      e match {
        case Left(e)  => e
        case Right(r) => throw new NoSuchElementException(s"Either.left.get on Left, $r")
      }
    }
  }

  implicit class RichEiter(r: Either[String, String]) {

    def shouldDeserializeTo[T: Decoder: ClassTag]: T =
      r.flatMap(parser.parse).flatMap(_.as[T]).get

    def shouldDeserializeToError: String = {
      parser.parse(r.getLeft).flatMap(_.as[ErrorOut]).get.error
    }
  }

  implicit class RichIO[T](t: IO[T]) {
    def unwrap: T = t.unsafeRunTimed(1.minute).get
  }
}