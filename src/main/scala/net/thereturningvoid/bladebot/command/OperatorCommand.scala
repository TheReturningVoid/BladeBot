package net.thereturningvoid.bladebot.command

import net.dv8tion.jda.MessageBuilder
import net.dv8tion.jda.entities.User
import net.dv8tion.jda.events.message.MessageReceivedEvent

import scala.collection.JavaConversions._

class OperatorCommand extends Command {

  override def getName: String = "Operator Management **[Operator]**"
  override def getDescription: String = "Commands to manage operators."
  override def getAliases: List[String] = List(s"${prefix}op", s"${prefix}operator")
  override def getUsage: String = s"${prefix}op *<command> [users]*\n" +
    s"${prefix}op getid *[users]* - Gets the user ID of the specified users. If no users are specified, it gets the sender's user ID.\n" +
    s"${prefix}op add *<users>* - Grants operator privileges to the mentioned users,\n" +
    s"${prefix}op remove *<users>* - Revokes operator privileges from the mentioned users.\n" +
     "  - The *getid* command does not require operator privileges to use." +
     "  - Users passed to these commands must be **mentioned** (ie.\"@username\", not \"username\".)\n" +
     "__Examples:__\n" +
    s"*${prefix}op getid @Joe Bloggs* - Gets the user ID of Joe Bloggs.\n" +
    s"*${prefix}op add @Foo Bar* - Grants operator privileges to Foo Bar.\n" +
    s"*${prefix}op remove @Joe Bloggs @Foo Bar* - Revokes operator privileges from Joe Bloggs and Foo Bar."

  override def onCommand(e: MessageReceivedEvent, args: Array[String]): Unit = {
    if (!args.isEmpty) {
      if (args.head.equalsIgnoreCase("getid")) {
        if (e.getMessage.getMentionedUsers.isEmpty) {
          e.getTextChannel.sendMessage(new MessageBuilder()
            .appendMention(e.getAuthor)
            .appendString(": Your User ID is ")
            .appendString(e.getAuthor.getId)
            .build())
        } else {
          e.getTextChannel.sendMessage(new MessageBuilder()
            .appendString("__IDs of mentioned users__")
            .build())
          e.getMessage.getMentionedUsers foreach { u =>
            e.getTextChannel.sendMessage(new MessageBuilder()
              .appendMention(u)
              .appendString(": ")
              .appendString(u.getId)
              .build())
          }
        }
      } else if (args.head.equalsIgnoreCase("add")) {
        //if (permissions.isOp(e.getAuthor)) {
          if (!e.getMessage.getMentionedUsers.isEmpty) {
            val m: MessageBuilder = new MessageBuilder
            for (i: Int <- e.getMessage.getMentionedUsers.indices) {
              val u: User = e.getMessage.getMentionedUsers()(i)
              permissions.addOp(u)
              permissions.getOps foreach(println(_))
              if (i == e.getMessage.getMentionedUsers.length - 1) {
                m.appendMention(u).appendString(": You are now an operator!")
              } else {
                m.appendMention(u).appendString(", ")
              }
            }
            e.getTextChannel.sendMessage(m.build())
          } else {
            e.getTextChannel.sendMessage(new MessageBuilder()
              .appendString("No users specified!")
              .build())
          }
       // } else {
         // e.getTextChannel.sendMessage(new MessageBuilder()
          //  .appendMention(e.getAuthor)
          //  .appendString(": You are not an operator!")
          //  .build())
      //  }
      } else if (args.head.equalsIgnoreCase("remove")) {
        if (permissions.isOp(e.getAuthor)) {
          if (!e.getMessage.getMentionedUsers.isEmpty) {
            val m: MessageBuilder = new MessageBuilder
            for (i: Int <- e.getMessage.getMentionedUsers.indices) {
              val u: User = e.getMessage.getMentionedUsers()(i)
              permissions.removeOp(u)
              if (i == e.getMessage.getMentionedUsers.length) {
                m.appendMention(u).appendString(": You are no longer operators!")
              } else {
                m.appendMention(u).appendString(", ")
              }
            }
            e.getTextChannel.sendMessage(m.build())
          } else {
            e.getTextChannel.sendMessage(new MessageBuilder()
              .appendString("No users specified!")
              .build())
          }
        } else {
          e.getTextChannel.sendMessage(new MessageBuilder()
            .appendMention(e.getAuthor)
            .appendString(": You are not an operator!")
            .build())
        }
      }
    } else {
      e.getTextChannel.sendMessage(new MessageBuilder()
        .appendString("No arguments were specified!")
        .build())
    }
  }

}
