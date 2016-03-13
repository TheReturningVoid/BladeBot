package net.thereturningvoid.bladebot

import scala.util.matching.Regex

object Predefs {

  implicit class Kestrel[T](val value: T) extends AnyVal {
    def tap[U](f: T => U) = { f(value); value }
  }

  implicit class RegexContext(val sc: StringContext) extends AnyVal {
    def r = new Regex(sc.parts.mkString, sc.parts.tail.map(_ => "x"):_*)
  }

}
