package net.thereturningvoid.bladebot.command

import net.dv8tion.jda.MessageBuilder
import net.dv8tion.jda.events.message.MessageReceivedEvent

import scala.collection.JavaConversions._

class AvatarCommand extends Command {

  override def getName: String = "Get User Avatars"
  override def getDescription: String = "Gets avatars of specified users."
  override def getAliases: List[String] = List(s"${prefix}avatar")
  override def getUsage: String = s"${prefix}avatar [users]\n" +
    s"${prefix}avatar - Sends your avatar to the text channel.\n" +
    s"${prefix}avatar *[users]* - Sends the avatars of mentioned users to the text channel.\n" +
     "  - Users passed to this command must be **mentioned** (ie. \"@username\", not \"username\").\n" +
     "__Examples:__\n" +
    s"*${prefix}avatar @Joe Bloggs @Foo Bar* - Posts Joe Bloggs' and Foo Bar's avatar."

  override def onCommand(e: MessageReceivedEvent, args: Array[String]): Unit = {
    if (!args.isEmpty) {
      e.getTextChannel.sendMessage(new MessageBuilder()
        .appendString("__Avatars of mentioned users__")
        .build())
      e.getMessage.getMentionedUsers foreach { u =>
        e.getTextChannel.sendMessage(new MessageBuilder()
          .appendMention(u)
          .appendString("'s avatar:\n")
          .appendString(u.getAvatarUrl)
          .build())
      }
    } else {
      e.getTextChannel.sendMessage(new MessageBuilder()
        .appendMention(e.getAuthor)
        .appendString("'s avatar is:\n")
        .appendString(e.getAuthor.getAvatarUrl)
        .build())
    }
  }

}
