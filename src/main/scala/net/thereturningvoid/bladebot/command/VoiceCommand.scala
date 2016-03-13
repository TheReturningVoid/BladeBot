package net.thereturningvoid.bladebot.command

import net.dv8tion.jda.MessageBuilder
import net.dv8tion.jda.entities.VoiceChannel
import net.dv8tion.jda.events.message.MessageReceivedEvent
import net.thereturningvoid.bladebot.song.SongQueue

import scala.collection.JavaConversions._

/**
  * Created by retvoid on 20/02/16.
  */
class VoiceCommand extends Command {

  override def getName: String = "Join/Leave Voice [Operator]"
  override def getDescription: String = "Join or leave a voice channel."
  override def getAliases: List[String] = List(s"${prefix}voice", s"${prefix}voicechannel", s"${prefix}vc")
  override def getUsage: String = s"${prefix}voice *[channel]* **OR** ${prefix}voicechannel *[channel]* **OR** ${prefix}vc *[channel]*\n" +
    s"${prefix}voice - Lists all available voice channels." +
    s"${prefix}voice *[channel]* - Connects to the specified voice channel." +
    "  - You can specify \"leave\" as the channel name to disconnect from voice." +
    "__Examples:__\n*" +
    prefix + "voice General* - Joins the \"General\" voice channel.\n" +
    s"*${prefix}voice leave* - Disconnects from all voice channels."

  override def onCommand(e: MessageReceivedEvent, args: Array[String]): Unit = {
    if (!args.isEmpty) {
      if (permissions.isOp(e.getAuthor)) {
        val channelToJoin: Option[VoiceChannel] = e.getGuild.getVoiceChannels collectFirst { case c if c.getName.equalsIgnoreCase(args.mkString(" ")) => c }
        if (channelToJoin.isDefined) {
          if (!e.getJDA.getAudioManager.isConnected) {
            e.getJDA.getAudioManager.openAudioConnection(channelToJoin.get)
            e.getJDA.getAudioManager.setSendingHandler(SongQueue.queuePlayer)
            e.getTextChannel.sendMessage(new MessageBuilder()
              .appendString("Joined \"" + channelToJoin.get.getName + "\".")
              .build())
          } else {
            val currentChannel: String = e.getJDA.getAudioManager.getConnectedChannel.getName
            e.getJDA.getAudioManager.moveAudioConnection(channelToJoin.get)
            e.getTextChannel.sendMessage(new MessageBuilder()
              .appendString("Moved from \"" + currentChannel + "\" to \"" + channelToJoin.get.getName + "\".")
              .build())
          }
        } else if (args.mkString(" ").equalsIgnoreCase("leave")) {
          e.getJDA.getAudioManager.closeAudioConnection()
          e.getTextChannel.sendMessage(new MessageBuilder()
            .appendString("Disconnected from voice channels.")
            .build())
        } else {
          e.getTextChannel.sendMessage(new MessageBuilder()
            .appendString("\"" + args.mkString(" ") + "\" is not a voice channel. Use *" + prefix + "voice* to show all voice channels.")
            .build())
        }
      } else {
        e.getTextChannel.sendMessage(new MessageBuilder()
          .appendMention(e.getAuthor)
          .appendString(": You are not an operator!")
          .build())
      }
    } else {
      val s: StringBuilder = new StringBuilder
      s.append("Voice channels:\n")
      e.getGuild.getVoiceChannels foreach { channel =>
        s.append("**" + channel.getName + "**\n")
      }
      e.getTextChannel.sendMessage(new MessageBuilder()
        .appendString(s.toString())
        .build())
    }
  }

}
