package net.thereturningvoid.bladebot.song

import org.json.JSONObject

import scala.util.Random

abstract class Song(val name: String, val artist: String, val id: String) {
  def get: JSONObject
}

case class SoundcloudSong(override val name: String, override val artist: String, override val id: String) extends Song(name, artist, id) {
  val songObject: JSONObject = new JSONObject().put("songName", name).put("artist", artist).put("id", id)

  def this(json: JSONObject) = this(
    if (json.has("error")) json.getString("error") else json.getString("title"),
    if (json.has("error")) "error" else json.getJSONObject("user").getString("username"),
    if (json.has("error")) "0" else Math.abs(Random.nextInt).toString)

  def get: JSONObject = songObject

  override def toString: String = songObject.toString(2)
}

object SoundcloudSong {
  def fromSongJSON(json: JSONObject): SoundcloudSong = {
    new SoundcloudSong(json.getString("songName"), json.getString("artist"), json.getString("id"))
  }
}