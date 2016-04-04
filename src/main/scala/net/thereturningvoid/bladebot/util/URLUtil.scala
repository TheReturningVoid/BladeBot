package net.thereturningvoid.bladebot.util

import java.net.{URLEncoder, MalformedURLException, URL}
import java.nio.charset.Charset


object URLUtil {

  def buildURL(url: String, charset: Charset, params: AnyRef*): Option[URL] = {
    try {
      Some(new URL(String.format(url, params map(p => URLEncoder.encode(p.toString, charset.name())):_*)))
    } catch {
      case e: MalformedURLException => e.printStackTrace(); None
    }
  }

  def getDomainName(url: URL): String = if (url.toURI.getHost.startsWith("www.")) url.toURI.getHost substring 4 else url.toURI.getHost

}
