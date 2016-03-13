package net.thereturningvoid.bladebot

import java.io.{BufferedWriter, IOException, File}
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path}

import net.dv8tion.jda.entities.{User, SelfInfo}
import net.thereturningvoid.bladebot.util.JSONUtil
import org.json.JSONArray

import scala.collection.mutable.ListBuffer
import scala.collection.JavaConversions._

class Permissions {
  private var ops: ListBuffer[String] = new ListBuffer[String]
  private val opsFile: Path = new File("ops.json").toPath

  try {
    if (!opsFile.toFile.exists()) {
      Files.createFile(opsFile)
      val w: BufferedWriter = Files.newBufferedWriter(opsFile, StandardCharsets.UTF_8)
      w.write(new JSONArray().toString(2))
      w.close()
    }
  } catch {
    case e: IOException =>
      println("IOException creating ops.json!")
      e.printStackTrace()
  }

  private val opsJson: JSONArray = new JSONArray(Files.readAllLines(opsFile, StandardCharsets.UTF_8).mkString("\n"))

  for (i: Int <- 0 to JSONUtil.jsonArrayToArray(opsJson).length) {
    if (!opsJson.isNull(i)) {
      ops += opsJson.getString(i)
    }
  }

  def setBotAsOp(bot: SelfInfo): Unit = ops + bot.getId

  def addOp(userID: String): Boolean = {
    if (!ops.contains(userID)) {
      opsJson.put(userID)
      ops += userID
      save()
      true
    } else false
  }
  def addOp(user: User): Boolean = addOp(user.getId)

  def removeOp(userID: String): Boolean = {
    if (!ops.contains(userID)) return false
    opsJson.remove(JSONUtil.jsonArrayToArray(opsJson).indexOf(userID))
    ops -= userID
    save()
    true
  }
  def removeOp(user: User): Boolean = removeOp(user.getId)

  private def save(): Unit = {
    try {
      val writer: BufferedWriter = Files.newBufferedWriter(opsFile, StandardCharsets.UTF_8)
      writer.write(opsJson.toString(2))
      writer.close()
    } catch {
      case e: IOException =>
        println("IOException saving ops.json!")
        e.printStackTrace()
    }
  }

  def getOps: List[String] = ops.clone().toList

  def isOp(userID: String): Boolean = ops.contains(userID)
  def isOp(user: User): Boolean = isOp(user.getId)
}

object Permissions {

  private var _permissions: Permissions = _

  def setupPermissions(): Unit = {
    if (_permissions != null) throw new RuntimeException("Why are we setting up permissions again?")
    _permissions = new Permissions
  }

  def getPermissions: Permissions = _permissions

}
