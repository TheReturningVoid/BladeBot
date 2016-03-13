package net.thereturningvoid.bladebot.util

import org.json.JSONArray

import scala.collection.mutable

object JSONUtil {

  def jsonArrayToArray(array: JSONArray): Array[String] = {
    if (array.length() <= 0) Array[String]() else {
      val list: mutable.MutableList[String] = new mutable.MutableList[String]
      for (i: Int <- 0 until array.length()) {
        list + array.get(i).toString
      }
      list.toArray
    }
  }

}
