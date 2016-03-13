package net.thereturningvoid.bladebot.util

object NumberUtil {

  def isFloat(str: String): Boolean = {
    try {
      java.lang.Float.parseFloat(str)
      true
    } catch {
      case e: NumberFormatException => false
    }
  }
}