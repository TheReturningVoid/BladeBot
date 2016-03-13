package net.thereturningvoid.bladebot

import java.io.{BufferedReader, IOException, BufferedWriter, File}
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path}

import com.google.gson.{GsonBuilder, Gson}

/**
  * Mainly taken from DV8FromTheWorld's Yui (https://github.com/DV8FromTheWorld/Yui/blob/master/src/main/java/net/dv8tion/discord/Settings.java),
  * who took it from Smbarbour's RavenBot (https://github.com/MCUpdater/RavenBot/blob/master/src/main/java/org/mcupdater/ravenbot/Settings.java).
  * They both used Java, so I rewrote in in Scala.
  */
class SettingsManager {
  private final val gson: Gson = new GsonBuilder().setPrettyPrinting().create()
  private final val configFile: Path = new File(".").toPath.resolve("config.json")
  private final val DEFAULT_PREFIX: String = ">"

  private var _settings: Settings = _

  if (!configFile.toFile.exists()) {
    println("SystemManager is creating default settings...")
    println("You will need to need to populate the config with your own info.")
    _settings = getDefaultSettings
    saveSettings()
    System.exit(BladeBot.NEW_CONFIG)
  }

  def loadSettings(): Unit = {
    try {
      checkBadEscape(configFile)

      val reader: BufferedReader = Files.newBufferedReader(configFile, StandardCharsets.UTF_8)
      _settings = gson.fromJson(reader, classOf[Settings])
      reader.close()
      println("Settings loaded!")
      checkOldSettingsFile()
    } catch {
      case e: IOException =>
        println("Error loading settings!")
        e.printStackTrace()
    }
  }

  def settings: Settings = _settings

  def saveSettings(): Unit = {
    val jsonOut: String = gson.toJson(this.settings)
    try {
      val writer: BufferedWriter = Files.newBufferedWriter(configFile, StandardCharsets.UTF_8)
      writer.append(jsonOut)
      writer.close()
    } catch {
      case e: IOException =>
        println("IOException saving settings!")
        e.printStackTrace()
    }
  }

  private def getDefaultSettings: Settings = {
    val newSettings: Settings = new Settings
    newSettings.email = "email"
    newSettings.password = "password"
    newSettings.proxyHost = ""
    newSettings.proxyPort = "8000"
    newSettings.scClientID = "Soundcloud Client ID (used for song requests)"
    newSettings.prefix = DEFAULT_PREFIX
    newSettings.songQueueChannel = "General"
    newSettings
  }

  private def checkOldSettingsFile(): Unit = {
    var modified: Boolean = false
    val defaults: Settings = getDefaultSettings
    if (settings.email == null) {
      settings.email = defaults.email
      modified = true
    }
    if (settings.password == null) {
      settings.password = defaults.password
      modified = true
    }
    if (settings.proxyHost == null) {
      settings.proxyHost = defaults.proxyHost
      modified = true
    }
    if (settings.proxyPort == null) {
      settings.proxyPort = defaults.proxyPort
      modified = true
    }
    if (settings.scClientID == null) {
      settings.scClientID = defaults.scClientID
      modified = true
    }
    if (settings.prefix == null) {
      settings.prefix = defaults.prefix
      modified = true
    }
    if (settings.songQueueChannel == null) {
      settings.songQueueChannel = defaults.songQueueChannel
      modified = true
    }

    if (modified) saveSettings()
  }

  @throws(classOf[IOException])
  private def checkBadEscape(path: Path): Unit = {
    val FORWARD_SOLIDUS: Byte = 47    // /
    val BACKWARD_SOLIDUS: Byte = 92   // \

    var modified: Boolean = false
    val bytes: Array[Byte] = Files.readAllBytes(path)
    for (i: Int <- bytes.indices) {
      if (bytes(i) == FORWARD_SOLIDUS) {
        modified = true
        bytes(i) = BACKWARD_SOLIDUS
      }
    }
    if (modified) Files.write(path, bytes)
  }

}

object SettingsManager {
  private var instance: Option[SettingsManager] = None

  def getInstance: SettingsManager = {
    if (instance.isEmpty) instance = Some(new SettingsManager)
    instance.get
  }
}