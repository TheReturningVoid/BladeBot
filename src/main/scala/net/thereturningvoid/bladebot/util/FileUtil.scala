package net.thereturningvoid.bladebot.util

import java.io.{File, StringWriter, InputStream}
import java.nio.file.{Paths, Files}

import org.apache.commons.io.IOUtils

import collection.JavaConversions._

object FileUtil {

  def inputStreamToString(is: InputStream): String = {
    val writer: StringWriter = new StringWriter()
    IOUtils.copy(is, writer)
    writer.toString
  }

  def isEmpty(file: File): Boolean = !(Files.readAllLines(file.toPath) exists(s => s != null && !s.trim.isEmpty))
}
