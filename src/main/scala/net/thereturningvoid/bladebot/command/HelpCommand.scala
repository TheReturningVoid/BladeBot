package net.thereturningvoid.bladebot.command

import net.dv8tion.jda.MessageBuilder
import net.dv8tion.jda.entities.PrivateChannel
import net.dv8tion.jda.events.message.MessageReceivedEvent

import scala.collection.mutable.ListBuffer

class HelpCommand extends Command {
  private final val NO_NAME: String = "No name provided."
  private final val NO_DESCRIPTION: String = "No description provided."
  private final val NO_USAGE: String = "No usage instructions provided."

  private val commands: ListBuffer[Command] = new ListBuffer[Command]

  override def getAliases: List[String] = List(s"${prefix}help", s"${prefix}commands")
  override def getName: String = "Help"
  override def getDescription: String = "List all other commands."
  override def getUsage: String = s"${prefix}help **OR** ${prefix}help *<command>*\n" +
    s"${prefix}help - Gives a list of all commands with a simple description.\n" +
    s"${prefix}help *<command>* - Gives the name, description, aliases and usage of a command.\n" +
    "  - This can use a command's alias in place of it's actual name.\n" +
    s"__Example:__\n*${prefix}help addSong* - Gets the command help for the addSong command."

  def registerCommand(command: Command): Command = {
    commands += command
    command
  }

  override def onCommand(e: MessageReceivedEvent, args: Array[String]): Unit = {
    if (!e.isPrivate) {
      e.getTextChannel.sendMessage(new MessageBuilder()
        .appendMention(e.getAuthor)
        .appendString(": Help information sent as a PM.")
        .build())
    }
    sendPrivate(e.getAuthor.getPrivateChannel, args)
  }

  private def sendPrivate(channel: PrivateChannel, args: Array[String]): Unit = {
    if (args.length < 1) {
      val s: StringBuilder = new StringBuilder
      commands foreach { c =>
        val description: String = if (isEmptyString(c.getDescription)) NO_DESCRIPTION else c.getDescription
        s.append("**").append(c.getAliases.head).append("**: ").append(description).append("\n")
      }

      channel.sendMessage(new MessageBuilder()
        .appendString("__All bot commands:__\n")
        .appendString(s.toString())
        .build())
    } else {
      val command: String = if (args(0).charAt(0) == prefix.head) args(1) else prefix + args(0)
      for (c: Command <- commands) {
        if (c.getAliases.contains(command)) {
          val name: String = if (isEmptyString(c.getName)) NO_NAME else c.getName
          val description: String = if (isEmptyString(c.getDescription)) NO_DESCRIPTION else c.getDescription
          val usage: String = if (isEmptyString(c.getUsage)) NO_USAGE else c.getUsage

          channel.sendMessage(new MessageBuilder()
            .appendString(s"**$name**:\n")
            .appendString(description + "\n")
            .appendString(s"**Usage**: $usage")
            .build())
          return
        }
      }
      channel.sendMessage(new MessageBuilder()
        .appendString("The provided command " + prefix + args(0) + "does not exist. Use " + prefix + "help to list all commands.")
        .build())
    }
  }

  private def isEmptyString(s: String): Boolean = s == null || s.isEmpty
}
