package net.thereturningvoid.bladebot.util

object NumberUtil {

  def isFloat(str: String): Boolean = {
    if (str.isEmpty || str == "") return false
    try {
      java.lang.Float.parseFloat(str)
      true
    } catch {
      case e: NumberFormatException => false
    }
  }

  def isInt(str: String): Boolean = {
    if (str.isEmpty || str == "") return false
    try {
      java.lang.Integer.parseInt(str)
      true
    } catch {
      case e: NumberFormatException => false
    }
  }
}