package net.thereturningvoid.bladebot.command

import net.dv8tion.jda.MessageBuilder
import net.dv8tion.jda.entities.Message
import net.dv8tion.jda.events.message.MessageReceivedEvent
import net.dv8tion.jda.hooks.ListenerAdapter
import net.thereturningvoid.bladebot.{Permissions, Settings, SettingsManager}

abstract class Command extends ListenerAdapter {

  protected val prefix: String = SettingsManager.getInstance.settings.prefix
  protected val settings: Settings = SettingsManager.getInstance.settings
  protected val permissions: Permissions = Permissions.getPermissions

  def onCommand(e: MessageReceivedEvent, args: Array[String])
  def getAliases: List[String]
  def getDescription: String
  def getName: String
  def getUsage: String

  override def onMessageReceived(e: MessageReceivedEvent): Unit = if (containsCommand(e.getMessage)) onCommand(e, commandArgs(e.getMessage).drop(1))

  protected def containsCommand(message: Message): Boolean = getAliases.map(_.toLowerCase).contains(commandArgs(message).headOption.map(_.toLowerCase).getOrElse(""))

  protected def commandArgs(message: Message): Array[String] = commandArgs(message.getContent)
  protected def commandArgs(string: String): Array[String] = string.split(" ")

  protected def sendMessage(e: MessageReceivedEvent, message: Message): Message = if (e.isPrivate) e.getPrivateChannel.sendMessage(message) else e.getChannel.sendMessage(message)
  protected def sendMessage(e: MessageReceivedEvent, message: String): Message = sendMessage(e, new MessageBuilder().appendString(message).build())
}
