package net.thereturningvoid.bladebot

import scala.collection.TraversableLike
import scala.collection.generic.CanBuildFrom
import scala.util.matching.Regex

object Predefs {

  // Perform functions that manipulate an object and return Unit, then return the object.
  implicit class Kestrel[T](val value: T) extends AnyVal {
    def tap[U](f: T => U) = { f(value); value }
  }

  // Regex string interpolation
  implicit class RegexContext(val sc: StringContext) extends AnyVal {
    def r = new Regex(sc.parts.mkString, sc.parts.tail.map(_ => "x"):_*)
  }

  // Allow for dropAt on collections
  implicit class TraversbleDropAt[A, Repr <: Traversable[A]](val xs: TraversableLike[A, Repr]) extends AnyVal {
    def dropAt[That](n: Int)(implicit cbf: CanBuildFrom[Repr, A, That]): That = {
      val bf = cbf()
      bf.sizeHint(xs.size - 1)
      bf ++= xs.take(n)
      bf ++= xs.drop(n + 1)
      bf.result()
    }
  }

}
