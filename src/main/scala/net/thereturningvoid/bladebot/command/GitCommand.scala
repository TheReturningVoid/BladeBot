package net.thereturningvoid.bladebot.command

import net.dv8tion.jda.MessageBuilder
import net.dv8tion.jda.events.message.MessageReceivedEvent

class GitCommand extends Command {

  override def getName: String = "Github Repository"
  override def getDescription: String = "Gives a link to this bot's Github repository."
  override def getAliases: List[String] = List(s"${prefix}git")
  override def getUsage: String = s"${prefix}git"

  override def onCommand(e: MessageReceivedEvent, args: Array[String]): Unit = {
    e.getChannel.sendMessage(new MessageBuilder()
      .appendString("http://www.github.com/TheReturningVoid/BladeBot")
      .build())
  }
}
