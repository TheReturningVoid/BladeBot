package net.thereturningvoid.bladebot.soundcloud

import java.io.{InputStream, IOException}
import java.net._
import java.nio.charset.{Charset, StandardCharsets}

import net.thereturningvoid.bladebot.SettingsManager
import org.apache.commons.io.IOUtils
import org.apache.commons.validator.routines.UrlValidator

import net.thereturningvoid.bladebot.util.URLUtil
import play.api.libs.json._

import scala.util.Try

class SCTrack(json: JsObject, val error: SCResult.Value) {

  private val scClientID: String = URLEncoder.encode(SettingsManager.getInstance.settings.scClientID, StandardCharsets.UTF_8.name)

  lazy val id = (json \ "id").as[Int]
  lazy val createdAt = (json \ "created_at").asOpt[String]
  lazy val userID = (json \ "user_id").asOpt[Int]
  lazy val userUri = (json \ "user" \ "uri").as[String]
  lazy val title = (json \ "title").as[String]
  lazy val permalink = (json \ "permalink").asOpt[String]
  lazy val permalinkUrl = (json \ "permalink_url").asOpt[String]
  lazy val uri = (json \ "uri").asOpt[String]
  lazy val sharing = (json \ "sharing").asOpt[String]
  lazy val embeddableBy = (json \ "embeddable_by").asOpt[String]
  lazy val purchaseUrl = (json \ "purchase_url").asOpt[String]
  lazy val artworkUrl = (json \ "artwork_url").asOpt[String]
  lazy val description = (json \ "description").asOpt[String]
  lazy val label = json \ "label"
  lazy val duration = (json \ "duration").asOpt[Int]
  lazy val genre = (json \ "genre").asOpt[String]
  lazy val tagList = (json \ "tag_list").validate[List[String]] map(s => s.map(_.split("([^\"]\\S*|\".+?\")\\s*").map(s => s.replace("\"", ""))))
  lazy val labelId = (json \ "label_id").asOpt[Int]
  lazy val labelName = (json \ "label_name").asOpt[String]
  lazy val release = (json \ "release").asOpt[Int]
  lazy val releaseDay = (json \ "release_day").asOpt[Int]
  lazy val releaseMonth = (json \ "release_month").asOpt[Int]
  lazy val releaseYear = (json \ "release_year").asOpt[Int]
  lazy val streamable = (json \ "streamable").as[Boolean]
  lazy val downloadable = (json \ "downloadable").asOpt[Boolean]
  lazy val state = (json \ "state").asOpt[String]
  lazy val license = (json \ "license").asOpt[String]
  lazy val trackType = (json \ "track_type").asOpt[String]
  lazy val waveformUrl = (json \ "waveform_url").asOpt[String]
  lazy val downloadUrl = (json \ "download_url").asOpt[String]
  lazy val streamUrl = (json \ "stream_url").as[String]
  lazy val videoUrl = (json \ "video_url").asOpt[String]
  lazy val bpm = (json \ "bpm").asOpt[Int]
  lazy val commentable = (json \ "commentable").asOpt[Boolean]
  lazy val isrc = (json \ "isrc").asOpt[String]
  lazy val keySignature = (json \ "key_signature").asOpt[String]
  lazy val commentCount = (json \ "comment_count").asOpt[Int]
  lazy val downloadCount = (json \ "download_count").asOpt[Int]
  lazy val playbackCount = (json \ "playback_count").asOpt[Int]
  lazy val favoritingsCount = (json \ "favoritings_count").asOpt[Int]
  lazy val originalFormat = (json \ "original_format").asOpt[String]
  lazy val originalContentSize = (json \ "original_content_size").asOpt[Int]
  lazy val createdWith = json \ "created_with"

  def artist: String = getUser.get.username.get

  def getUser: Option[SCUser] = try {
    val url: Option[URL] = URLUtil.buildURL(userUri + "?client_id=%s", StandardCharsets.UTF_8, scClientID)
    if (url.isDefined) {
      val urlConn: Option[URLConnection] = Try(Some(url.get.openConnection)) recover {
        case e: IOException => e.printStackTrace(); None
      } get;
      if (urlConn.isDefined) {
        val obj = Json.parse(urlConn.get.getInputStream)
        Some(new SCUser(obj.as[JsObject]))
      } else None
    } else None
  } catch {
    case e: MalformedURLException => e.printStackTrace(); None
    case e: IOException => e.printStackTrace(); None
  }

  def getStream: InputStream = {
    // Is the track streamable?
    if (streamable) {
      // Try to get the stream URL
      val stream: Option[URL] = Try(Option(new URL(streamUrl + s"?client_id=$scClientID"))) recover {
        case e: MalformedURLException => e.printStackTrace(); None
      } get;
      // Did we create the stream URL?
      if (stream.isDefined) {
        // Open a connection to the stream
        val streamConn: Option[URLConnection] = Try(Option(stream.get.openConnection)) recover {
          case e: IOException => e.printStackTrace(); None
        } get;
        // Did we connect?
        if (streamConn.isDefined) {
          // Give the stream
          streamConn.get.getInputStream
        } else IOUtils.toInputStream(SCResult.SC_STREAM_URL_CONNECT_FAIL.id.toString)
      } else IOUtils.toInputStream(SCResult.SC_STREAM_URL_CREATE_FAIL.id.toString)
    } else {
      IOUtils.toInputStream(SCResult.SC_TRACK_NOT_STREAMABLE.id.toString)
    }
  }

  def toConciseJSON: JsObject = Json.obj(
    "title" -> title,
    "artist" -> artist,
    "id" -> id
  )
}

object SCTrack {
  private val charset: Charset = StandardCharsets.UTF_8
  private val scClientID: String = URLEncoder.encode(SettingsManager.getInstance.settings.scClientID, charset.name)
  private final val emptyJson: JsObject = Json.obj()

  def apply(url: String): SCTrack = {
    // Is the given URL valid?
    if (UrlValidator.getInstance().isValid(url)) {
      // Try to create the Soundcloud /resolve url
      val resolveUrl: Option[URL] = URLUtil.buildURL("http://api.soundcloud.com/resolve?url=%s&client_id=%s", charset, url, scClientID)
      // Did the creation succeed?
      if (resolveUrl.isDefined) {
        // Is the resulting URL an api.soundcloud.com one?
        if (URLUtil.getDomainName(resolveUrl.get) == "api.soundcloud.com") {
          // Try to open a connection to the /resolve url
          val resConn: Option[HttpURLConnection] = Try(Option(resolveUrl.get.openConnection().asInstanceOf[HttpURLConnection])) recover {
            case e: IOException => e.printStackTrace(); None
          } get;
          // Did we connect?
          if (resConn.isDefined) {
            // Set some connection paramaters
            resConn.get.setRequestProperty("Accept-Charset", charset.name)
            resConn.get.setRequestMethod("GET")
            resConn.get.connect()
            // Did we get a 302 response?
            if (resConn.get.getResponseCode == 302) {
              // Give the track object
              new SCTrack(Json.parse(new URL(resConn.get.getHeaderField("Location")).openStream()).as[JsObject], SCResult.SUCCESS)
            } else new SCTrack(emptyJson, SCResult.SC_RESOLVE_INVALID_RESPONSE_CODE)
          } else new SCTrack(emptyJson, SCResult.SC_RESOLVE_CONNECT_FAIL)
        } else new SCTrack(emptyJson, SCResult.SC_RESOLVE_CREATE_FAIL)
      } else new SCTrack(emptyJson, SCResult.SC_RESOLVE_CREATE_FAIL)
    } else new SCTrack(emptyJson, SCResult.SC_RESOLVE_CREATE_FAIL)
  }

  def fromConciseJSON(json: JsObject): SCTrack = SCTrack(s"http://api.soundcloud.com/tracks/${(json \ "id").as[Int]}?client_id=$scClientID")
}
