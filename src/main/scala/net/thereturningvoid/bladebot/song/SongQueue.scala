package net.thereturningvoid.bladebot.song

import java.io.{InputStream, BufferedWriter, File}
import java.nio.charset.StandardCharsets
import java.nio.file.{Path, Files}

import net.thereturningvoid.bladebot.util.EnumReflector
import org.apache.commons.io.FileUtils
import org.json.{JSONException, JSONArray}

import scala.util.Try
import collection.JavaConversions._

object SongQueue {

  val queuePlayer: QueuePlayer = new QueuePlayer()

  val songQueueFile: Path = new File("queue.json").toPath
  val songQueueDir: Path = new File("songqueue").toPath

  def addSongToQueue(song: Song, stream: InputStream): Option[SongResponse.Value] = {
    // Check if the song has an error
    if (song.get.getString("artist") == "error" && song.get.getString("id") == "0") {
      return Some(EnumReflector.withName[SongResponse.Value](song.get.getString("songName")))
    }

    // Define the song file
    val songFile: File = new File("songqueue/" + song.id + ".mp3")

    // Is the song already in the queue?
    if (getSongByName(song.name).isEmpty) {

      // Add the JSONObject to the main JSONArray, or create a new one if the song queue is empty
      val songsArray: JSONArray = getQueueJSONArray
      songsArray.put(song.get)

      // Write the resulting JSONArray to the queue file
      val writer: BufferedWriter = Files.newBufferedWriter(songQueueFile, StandardCharsets.UTF_8)
      writer.write(songsArray.toString(2))
      writer.close()

      // Create thread to download song
      new Thread(new Runnable {
        override def run(): Unit = {
          val istream: Option[InputStream] = Option(stream)
          if (istream.isDefined) {
            FileUtils.copyInputStreamToFile(istream.get, songFile)
          }
        }
      }).start()
      None
    } else Some(SongResponse.QUEUE_CONTAINS_SONG)
  }

  def removeSongFromQueue(song: Song): Option[SongResponse.Value] = {
    // Check if the song has an error
    if (song.get.getString("artist") == "error" && song.get.getString("id") == "0") {
      return Some(EnumReflector.withName[SongResponse.Value](song.get.getString("songName")))
    }

    // Is the song in the queue?
    if (getObjectsInArray(getQueueJSONArray).contains(song)) {
      // Create a new JSONArray and remove the song from it
      val songArray: JSONArray = getQueueJSONArray
      songArray.remove(getObjectsInArray(getQueueJSONArray).indexOf(song))

      // Update the song queue
      val writer: BufferedWriter = Files.newBufferedWriter(songQueueFile, StandardCharsets.UTF_8)
      writer.write(songArray.toString(2))
      writer.close()

      // Delete the song from the hard disk
      new File("songqueue/" + song.id + ".mp3").delete()
      None
    } else Some(SongResponse.QUEUE_DOES_NOT_CONTAIN_SONG)
  }

  def getSongAtIndex(index: Int): Option[Song] = Option(SoundcloudSong.fromSongJSON(getQueueJSONArray.getJSONObject(index)))
  def getTopSong: Option[Song] = getSongAtIndex(0)

  def getSongsByArtist(artist: String): List[Song] = getObjectsInArray(getQueueJSONArray).filter(s => s.artist == artist)
  def getSongByName(name: String): Option[Song] = getObjectsInArray(getQueueJSONArray).find(s => s.name == name)

  def getObjectsInArray(array: JSONArray): List[Song] =
    getQueueJSONArray.toList.zipWithIndex.map(t => SoundcloudSong.fromSongJSON(getQueueJSONArray.getJSONObject(t._2)))

  def getQueueJSONArray: JSONArray = (Try(new JSONArray(Files.readAllLines(songQueueFile).mkString("\n"))) recover {
    case e: JSONException => new JSONArray()
  }).get
}
