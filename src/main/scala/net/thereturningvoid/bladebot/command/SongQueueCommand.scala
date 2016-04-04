package net.thereturningvoid.bladebot.command

import java.io._
import javafx.scene.Scene

import net.dv8tion.jda.MessageBuilder
import net.dv8tion.jda.events.message.MessageReceivedEvent
import net.thereturningvoid.bladebot.song.SongQueue
import net.thereturningvoid.bladebot.soundcloud.{SCTrack, SCResult}
import net.thereturningvoid.bladebot.util.{FileUtil, NumberUtil}
import org.apache.commons.io.IOUtils

class SongQueueCommand extends Command {

  override def getName: String = "Song Queue Controls"
  override def getDescription: String = "Controls for BladeBot's song queue."
  override def getAliases: List[String] = List(s"${prefix}queue", s"${prefix}sq", s"${prefix}songqueue")
  override def getUsage: String = s"${prefix}queue *<command>* **OR** ${prefix}sq *<command>* **OR** ${prefix}songqueue *<command>*\n" +
    s"${prefix}queue add *<soundcloudLink>* - Adds a song from SoundCloud to the queue\n." +
    s"${prefix}queue volume - [OP only] Change the volume of the playing song.\n" +
    s"${prefix}queue toggle - [OP only] Toggle the song queue on or off.\n" +
    s"${prefix}queue pause - [OP only] Pause the song in the queue.\n" +
    s"  - *${prefix}queue toggle* can be used to unpause the queue.\n" +
     "  - The song queue only supports SoundCloud songs at the moment. YouTube support is coming soon.\n"
     "__Example:__\n" +
    s"""*${prefix}queue add https://soundcloud.com/karmafields/greatness* - Adds "Karma Fields - Greatness (feat Talib Kweli)" to the song queue."""

  override def onCommand(e: MessageReceivedEvent, args: Array[String]): Unit = {
    if (!args.isEmpty) {
      if (e.getJDA.getAudioManager.isConnected) {

        // Volume command
        if (args.head.equalsIgnoreCase("volume")) {
          if (permissions.isOp(e.getAuthor)) {
            val volume: Float = if (NumberUtil.isFloat(args(1))) args(1).toFloat / 100.0F else -1000.0F
            if (volume >= 0.0F && volume <= 1.0F) {
              SongQueue.queuePlayer.setVolume(volume)
              e.getTextChannel.sendMessage(new MessageBuilder()
                .appendString(s"Volume set to ${Math.ceil(volume * 100.0F)}.")
                .build())
            } else if (volume == -1000.0F) {
              e.getTextChannel.sendMessage(new MessageBuilder()
                .appendString(s""""${args(1)}" is not a number.""")
                .build())
            } else {
              e.getTextChannel.sendMessage(new MessageBuilder()
                .appendString("The volume must be a value between 0 and 100.")
                .build())
            }
          } else {
            e.getTextChannel.sendMessage(new MessageBuilder()
              .appendMention(e.getAuthor)
              .appendString(": You are not an operator!")
              .build())
          }

        // Pause command
        } else if (args.head.equalsIgnoreCase("pause")) {
          if (permissions.isOp(e.getAuthor)) {
            if (SongQueue.queuePlayer.isPlaying) {
              SongQueue.queuePlayer.pause()
              e.getTextChannel.sendMessage(new MessageBuilder()
                .appendString("Song player paused.")
                .build())
            } else {
              e.getTextChannel.sendMessage(new MessageBuilder()
                .appendString("The song player is already not playing!")
                .build())
            }
          } else {
            e.getTextChannel.sendMessage(new MessageBuilder()
              .appendMention(e.getAuthor)
              .appendString(": You are not an operator!")
              .build())
          }

        // Add song command
        } else if (args.head.equalsIgnoreCase("add")) {
          val song: SCTrack = SCTrack(args(1))
          if (!hasError(song.error.id)) {
            val stream: Array[Byte] = IOUtils.toByteArray(song.getStream)
            val streamStr: String = new String(stream)
            val streamError: Int = if (NumberUtil.isInt(streamStr)) streamStr.toInt else 0
            if (!hasError(streamError)) {
              SongQueue.addSongToQueue(song, stream)
              val pos: Int = SongQueue.getObjectsInArray(SongQueue.getQueueJSONArray).map(s => s.title).indexOf(song.title)
              e.getTextChannel.sendMessage(new MessageBuilder()
                .appendString(s"Added ${song.title} to position $pos of queue!")
                .build())
            }
          }

        // Queue on/off command
        } else if (args.head.equalsIgnoreCase("toggle")) {
          if (permissions.isOp(e.getAuthor)) {
            if (SongQueue.queuePlayer.isPlaying) {
              SongQueue.queuePlayer.forceStop()
              e.getTextChannel.sendMessage(new MessageBuilder()
                .appendString("Song queue stopped!")
                .build())
            } else {
              SongQueue.queuePlayer.setChannel(e.getTextChannel)
              SongQueue.queuePlayer.startQueue()
              e.getTextChannel.sendMessage(new MessageBuilder()
                .appendString("Song queue started!")
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
          .appendString(s"""Not connected to a voice channel! Use "${prefix}voice" to join a channel.""")
          .build())
      }
    } else {
      e.getTextChannel.sendMessage(new MessageBuilder()
        .appendString("No arguments were specified!")
        .build())
    }

    def hasError(error: Int): Boolean = {
      if (error == SCResult.SC_RESOLVE_CREATE_FAIL.id) {
        e.getTextChannel.sendMessage(new MessageBuilder()
          .appendString("The url given was invalid (or at least made the SoundCloud API request invalid)! Make sure the URL is correct.")
          .build())
        true
      } else if (error == SCResult.SC_RESOLVE_CONNECT_FAIL.id) {
        e.getTextChannel.sendMessage(new MessageBuilder()
          .appendString("Could not connect to the Soundcloud API! Try requesting again at a later time.")
          .build())
        true
      } else if (error == SCResult.SC_RESOLVE_INVALID_RESPONSE_CODE.id) {
        e.getTextChannel.sendMessage(new MessageBuilder()
          .appendString("The response was not what was expected (302)! You probably provided a non-Soundcloud link.")
          .build())
        true
      } else if (error == SCResult.SC_TRACK_NOT_STREAMABLE.id) {
        e.getTextChannel.sendMessage(new MessageBuilder()
          .appendString("The requested song is not streamable! Try another song.")
          .build())
        true
      } else if (error == SCResult.SC_STREAM_URL_CREATE_FAIL.id) {
        e.getTextChannel.sendMessage(new MessageBuilder()
          .appendString("The url given was invalid (or at least made the stream request invalid)! Make sure the URL is correct.")
          .build())
        true
      } else if (error == SCResult.SC_STREAM_URL_CONNECT_FAIL.id) {
        e.getTextChannel.sendMessage(new MessageBuilder()
          .appendString("Could not connect to the track stream! Try requesting the song again.")
          .build())
        true
      } else false
    }
  }

}
