package net.thereturningvoid.bladebot.song

import java.io.File

import net.dv8tion.jda.MessageBuilder
import net.dv8tion.jda.audio.player.FilePlayer
import net.dv8tion.jda.entities.TextChannel
import net.thereturningvoid.bladebot.BladeBot

class QueuePlayer extends FilePlayer {

  var currentSong: Option[Song] = None
  var channel: Option[TextChannel] = None
  var volume: Float = 0.2F

  def getCurrentSong: Option[Song] = currentSong

  def setChannel(c: TextChannel): Unit = this.channel = Some(c)

  def startQueue(): Unit = {
    currentSong = SongQueue.getTopSong
    println(currentSong.get.name)
    setAudioFile(new File("songqueue/" + currentSong.get.id + ".mp3"))
    Thread.sleep(50) // this is pretty hacky
    play()
  }

  override def play(): Unit = {
    if (currentSong.isDefined) {
      setVolume(volume)
      super.play()
      SongQueue.removeSongFromQueue(currentSong.get)
      channel.get.sendMessage(new MessageBuilder()
        .appendString("\uD83C\uDFB5 Now Playing: " + currentSong.get.name + " by " + currentSong.get.artist)
        .build())
    }
  }

  override def stop(): Unit = {
    super.stop()
    volume = amplitude
    startQueue()
  }

  def forceStop(): Unit = super.stop()

}
