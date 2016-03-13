package net.thereturningvoid.bladebot.util

import java.io.{IOException, InputStream}
import java.net._
import java.nio.charset.StandardCharsets

import net.dv8tion.jda.entities.TextChannel
import net.thereturningvoid.bladebot.SettingsManager
import net.thereturningvoid.bladebot.song.SongResponse
import net.thereturningvoid.bladebot.util.FileUtil._
import org.apache.commons.io.IOUtils
import org.json.JSONObject

import scala.util.Try

object SoundcloudUtil {

  private val charset: String = StandardCharsets.UTF_8.name() // Use the UTF-8 charset
  private val scClientID: String = URLEncoder.encode(SettingsManager.getInstance.settings.scClientID, charset)

  def getSoundcloudTrackObject(url: String): JSONObject = {
    // Try to create the Soundcloud /resolve URL
    val resolveURL: URL = Try(new URL(
      String.format("http://api.soundcloud.com/resolve?url=%s&client_id=%s",
      URLEncoder.encode(url, charset),
      scClientID))) getOrElse new URL("http://www.invalid.webpage/")
    // Is the supplied link a soundcloud one?
    if (getDomainName(resolveURL) == "api.soundcloud.com") {
      // Try to open the connection to /resolve
      val resolveConnection: Option[HttpURLConnection] = (Try(Option(resolveURL.openConnection().asInstanceOf[HttpURLConnection])) recover {
        case e: IOException => println("IOException connecting to the Soundcloud /resolve endpoint!"); e.printStackTrace(); None
      }).get
      if (resolveConnection.isDefined) {
        val resConn: HttpURLConnection = resolveConnection.get
        // Set some connection parameters
        resConn.setRequestProperty("Accept-Charset", charset)
        resConn.setRequestMethod("GET")
        resConn.connect()
        // Did we get a 302 response?
        if (resConn.getResponseCode == 302) {
          // Give the /track object
          new JSONObject(inputStreamToString(new URL(resConn.getHeaderField("Location")).openStream()))
        } else {
          new JSONObject().put("error", SongResponse.SC_RESOLVE_RESPONSE_NOT_302.toString)
        }
      } else {
        new JSONObject().put("error", SongResponse.SC_RESOLVE_IOEXCEPTION.toString)
      }
    } else new JSONObject().put("error", SongResponse.SC_RESOLVE_BAD_URL.toString)
  }

  def getSoundcloudSongStream(trackObject: JSONObject): InputStream = {
    // Is there an error in the track object?
    if (trackObject.has("error")) {
      return IOUtils.toInputStream(trackObject.getString("error"))
    }

    // Is the track streamable?
    if (trackObject.getBoolean("streamable")) {
      // Try to get the stream link
      val streamLink: String = Try(trackObject.getString("stream_url") + s"?client_id=$scClientID") getOrElse ""
      if (!streamLink.isEmpty) {
        // Try to create a connection to the stream
        val streamConnection: Option[URLConnection] = (Try(Option(new URL(streamLink).openConnection)) recover {
          case e: MalformedURLException =>
            println("An error occurred getting the song!")
            println("Details: MalformedURLException thrown connecting to stream.")
            e.printStackTrace()
            None
          case e: IOException =>
            println("An error occurred getting the song!")
            println("Details: IOException thrown connecting to stream.")
            e.printStackTrace()
            None
        }).get
        if (streamConnection.isDefined) {
          // Return the stream connection's InputStream
          streamConnection.get.getInputStream
        } else IOUtils.toInputStream(SongResponse.SC_STREAM_CANNOT_CONNECT.toString)
      } else {
        println("An error occurred getting the song!")
        println("Details: Could not get \"stream_url\" of track object.")
        IOUtils.toInputStream(SongResponse.SC_STREAM_MISSING_STREAM_URL.toString)
      }
    } else IOUtils.toInputStream(SongResponse.SC_STREAM_NOT_STREAMABLE.toString)
  }

  private def getDomainName(url: URL): String = if (url.toURI.getHost.startsWith("www.")) url.toURI.getHost substring 4 else url.toURI.getHost

}
