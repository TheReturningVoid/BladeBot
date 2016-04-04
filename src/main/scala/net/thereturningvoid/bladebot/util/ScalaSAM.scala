package net.thereturningvoid.bladebot.util

import java.util.function.{Supplier, Consumer}

object ScalaSAM {

  def Consumer[T](f: T => Unit) = new Consumer[T] {
    override def accept(t: T) = f(t)
  }

  def Supplier[T](f: Unit => T) = new Supplier[T] {
    override def get() = f.apply(())
  }

}
