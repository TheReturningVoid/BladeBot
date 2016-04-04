package net.thereturningvoid.bladebot.song

import java.io.{InputStream, BufferedWriter, File}
import java.nio.charset.StandardCharsets
import java.nio.file.{Path, Files}

import net.thereturningvoid.bladebot.soundcloud.SCTrack
import net.thereturningvoid.bladebot.soundcloud.SCResult
import net.thereturningvoid.bladebot.Predefs._
import org.apache.commons.io.{IOUtils, FileUtils}
import org.json.{JSONException, JSONArray}
import play.api.libs.json.{JsObject, JsArray, Json}
import sun.nio.ch.IOUtil

import scala.util.Try
import collection.JavaConversions._

object SongQueue {

  val queuePlayer: QueuePlayer = new QueuePlayer()

  val songQueueFile: Path = new File("queue.json").toPath
  val songQueueDir: Path = new File("songqueue").toPath

  def addSongToQueue(track: SCTrack, stream: Array[Byte]): Option[SCResult.Value] = {
    // Check if the song has an error
    if (track.error.id != 0) return Some(track.error)

    // Define the song file
    val songFile: File = new File("songqueue/" + track.id + ".mp3")

    // Is the song already in the queue?
    if (getTrackByName(track.title).isEmpty) {

      // Add the JSONObject to the main JSONArray, or create a new one if the song queue is empty
      val songsArray: JsArray = getQueueJSONArray

      // Write the resulting JSONArray to the queue file
      val writer: BufferedWriter = Files.newBufferedWriter(songQueueFile, StandardCharsets.UTF_8)
      writer.write(Json.prettyPrint(songsArray :+ track.toConciseJSON))
      writer.close()

      // Create thread to download song
      new Thread(new Runnable {
        override def run(): Unit = {
          if (stream != null) {
            FileUtils.writeByteArrayToFile(songFile, stream)
          }
        }
      }).start()
      None
    } else Some(SCResult.QUEUE_CONTAINS_SONG)
  }

  def removeSongFromQueue(track: SCTrack): Option[SCResult.Value] = {
    // Check if the song has an error
    if (track.error.id != 0) return Some(track.error)

    // Is the song in the queue?
    if (getObjectsInArray(getQueueJSONArray).contains(track)) {
      // Create a new JSONArray and remove the song from it
      val songArray: JsArray = getQueueJSONArray.value.dropAt(getObjectsInArray(getQueueJSONArray).indexOf(track)).foldLeft(JsArray())((arr, v) => arr :+ v)

      // Update the song queue
      val writer: BufferedWriter = Files.newBufferedWriter(songQueueFile, StandardCharsets.UTF_8)
      writer.write(Json.prettyPrint(songArray))
      writer.close()

      // Delete the song from the hard disk
      new File("songqueue/" + track.id + ".mp3").delete()
      None
    } else Some(SCResult.QUEUE_SONG_NOT_FOUND)
  }

  def getQueueJSONArray: JsArray = Json.parse(Files.readAllLines(songQueueFile).mkString("\n")).as[JsArray]

  def getObjectsInArray(array: JsArray): List[SCTrack] = getQueueJSONArray.as[Seq[JsObject]].map(o => SCTrack.fromConciseJSON(o)).toList

  def getTrackByName(name: String): Option[SCTrack] = getObjectsInArray(getQueueJSONArray).find(s => s.title == name)
  def getSongsByArtist(artist: String): List[SCTrack] = getObjectsInArray(getQueueJSONArray).filter(s => s.artist == artist)

  def getSongAtIndex(index: Int): Option[SCTrack] = Option(getObjectsInArray(getQueueJSONArray)(index))
  def getTopSong: Option[SCTrack] = getSongAtIndex(0)
}
