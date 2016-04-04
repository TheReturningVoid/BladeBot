package net.thereturningvoid.bladebot

import java.io.{IOException, File, UnsupportedEncodingException}
import java.net.URLDecoder
import javax.security.auth.login.LoginException

import jline.console.ConsoleReader
import jline.console.history.FileHistory
import net.dv8tion.jda.entities.{Message, TextChannel, Guild}
import net.dv8tion.jda.utils.InviteUtil
import net.dv8tion.jda.{MessageBuilder, JDABuilder, JDA}
import net.thereturningvoid.bladebot.command._
import net.thereturningvoid.bladebot.Predefs._
import net.thereturningvoid.bladebot.util.ScalaSAM._
import org.apache.commons.lang3.StringUtils

import scala.collection.JavaConversions._

object BladeBot {

  // No error exit codes
  final val NORMAL_SHUTDOWN = 10
  final val RESTART = 11
  final val NEW_CONFIG = 12

  // Error exit codes
  final val DISCORD_CANNOT_CONNECT = 20
  final val NO_LOGIN = 21
  final val BAD_LOGIN = 22

  var jda: Option[JDA] = None
  var permissions: Option[Permissions] = None

  private val history: FileHistory = new FileHistory(new File(".history").getAbsoluteFile)
  private lazy val reader = new ConsoleReader() tap { r =>
    r.setHistory(history)
    r.setBellEnabled(false)
    r.setExpandEvents(false)
    r.setPrompt("bladebot> ")
  }

  def main(args: Array[String]): Unit = {
    if (System.getProperty("file.encoding").equals("UTF-8")) {
      setupBot()
      println("REPL active.")
      while (evalCommand(read()).map(println).isDefined) { }
      System.exit(0)
    } else {
      relaunchInUTF8()
    }
  }

  def getThisJarFile: File = {
    val path: String = BladeBot.getClass.getProtectionDomain.getCodeSource.getLocation.getPath
    val decodedPath: String = URLDecoder.decode(path, "UTF-8")

    if (!decodedPath.endsWith(".jar")) {
      new File("BladeBot.jar")
    }
    new File(decodedPath)
  }

  def getJDA: JDA = jda.get
  def getPermissions: Permissions = permissions.get

  private def read(): String = {
    println()
    reader.readLine() tap { in => println() }
  }

  private def evalCommand(in: String): Option[String] = {
    val input: Array[String] = in.split(" ")
    input.head.toLowerCase match {
      case "" => Some("Invalid command. Try \"help\" for more info.")
      case "help" => Some(helpInfo())
      case "addop" =>
        try {
          if (getPermissions.addOp(jda.get.getUsersByName(input(1)).head))
            Some(jda.get.getUsersByName(input(1)).head.getUsername + " added to ops!")
          else Some(jda.get.getUsersByName(input(1)).head.getUsername + " is already an op!")
        } catch {
          case e: NoSuchElementException => Some("The given user does not exist!")
        }
      case "acceptinvite" =>
        println(input(1))
        InviteUtil.join(InviteUtil.resolve(input(1)), jda.get, Consumer[Guild](g => println("Joined the \"" + g.getName + "\" guild!")))
        Some("")
      case "setplaying" =>
        val game: String = input.drop(1).mkString(" ")
        getJDA.getAccountManager.setGame(game)
        Some("Set game to \"" + game + "\"!")
      case "say" =>
        val sayReader: ConsoleReader = new ConsoleReader()
        val guild: Guild = getJDA.getGuildsByName(sayReader.readLine("Enter the name of the guild to send the message in: ")).head
        val channelName: String = sayReader.readLine("Enter the channel to send the message in: ").toLowerCase
        val channel: TextChannel = guild.getTextChannels.find(_.getName.toLowerCase == channelName).get
        channel.sendMessage(new MessageBuilder()
          .appendString(sayReader.readLine("Enter the message to send: "))
          .build())
        Some("Message sent!")
      case r"exit|quit|stop" =>
        reader.getHistory.asInstanceOf[FileHistory].flush()
        None
      case _ => Some("Invalid command. Try \"help\" for more info.")
    }
  }

  private def helpInfo(): String = "BladeBot Console Help\n\n" +
    "addop <user>: Adds the specified user as a bot operator.\n" +
    "acceptInvite <invite>: Joins the server with the given invite.\n" +
    "setplaying <game>: Sets the playing game to the given name.\n" +
    "say <guild> <channel> <text>: Says a message in the specified guild and channel."

  private def setupBot(): Unit = {
    try {
      // Define the settings instance
      val settingsManager: SettingsManager = SettingsManager.getInstance
      settingsManager.loadSettings()
      val settings: Settings = SettingsManager.getInstance.settings

      // Setup JDA instance
      val jdaBuilder: JDABuilder = new JDABuilder(settings.email, settings.password)
      // Setup operator list
      Permissions.setupPermissions()
      permissions = Some(Permissions.getPermissions)

      // Register commands
      val help: HelpCommand = new HelpCommand
      jdaBuilder.addListener(help.registerCommand(help))
      jdaBuilder.addListener(help.registerCommand(new VoiceCommand))
      jdaBuilder.addListener(help.registerCommand(new AvatarCommand))
      jdaBuilder.addListener(help.registerCommand(new OperatorCommand))
      jdaBuilder.addListener(help.registerCommand(new SongQueueCommand))
      jdaBuilder.addListener(help.registerCommand(new GitCommand))

      // Connect with proxy if one is specified
      if (settings.proxyHost != null && !settings.proxyHost.isEmpty) {
        jdaBuilder.setProxy(settings.proxyHost, settings.proxyPort.toInt)

        System.setProperty("http.proxyHost", settings.proxyHost)
        System.setProperty("http.proxyPort", settings.proxyPort)
        System.setProperty("https.proxyHost", settings.proxyHost)
        System.setProperty("https.proxyPort", settings.proxyPort)
      }

      // Set the bot as operator io itself
      jda = Some(jdaBuilder.buildBlocking())
      Permissions.getPermissions.setBotAsOp(jda.get.getSelfInfo)
    } catch {
      case e: IllegalArgumentException =>
        println("No login provided! Please specify a login in the config file.")
        System.exit(NO_LOGIN)
      case e: LoginException =>
        println("The login in the config file is incorrect.")
        println("Did you modify the config file after it was created?")
        System.exit(BAD_LOGIN)
      case e: InterruptedException =>
        println("The login thread was interrupted somehow!")
        System.exit(DISCORD_CANNOT_CONNECT)
    }
  }

  @throws(classOf[InterruptedException])
  @throws(classOf[UnsupportedEncodingException])
  private def relaunchInUTF8(): Unit = {
    println("[Launcher] We are not in UTF-8 mode! This is a problem!")
    println("[Launcher] Relaunching in UTF-8 mode with -Dfile.encoding=UTF-8")

    val command: Array[String] = Array("java", "-Dfile.encoding=UTF-8", "-jar", BladeBot.getThisJarFile.getAbsolutePath)

    val processBuilder: ProcessBuilder = new ProcessBuilder(command.toList)
    processBuilder.inheritIO() // Use the same command line

    try {
      val process: Process = processBuilder.start()
      process.waitFor()
      System.exit(process.exitValue())
    } catch {
      case e: IOException =>
        if (e.getMessage.contains("\"java\"")) {
          println("[Launcher] There was an error relaunching the bot. We couldn't find Java.")
          println("[Launcher] Tried relaunching with the command:\n  " + StringUtils.join(command, " ", 0, command.length))
          println("[Launcher] Make sure Java is properly defined in your PATH.")
          println("[Launcher] Terminating.")
        } else {
          println("[Launcher] An unknown IOException occured during relaunch!")
          e.printStackTrace()
        }
    }
  }

}