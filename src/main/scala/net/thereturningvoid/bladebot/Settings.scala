package net.thereturningvoid.bladebot

/*
 * Mainly taken from DV8FromTheWorld's Yui (https://github.com/DV8FromTheWorld/Yui/blob/master/src/main/java/net/dv8tion/discord/Settings.java),
 * who took it from Smbarbour's RavenBot (https://github.com/MCUpdater/RavenBot/blob/master/src/main/java/org/mcupdater/ravenbot/Settings.java).
 * They both used Java, so I rewrote in in Scala.
 */
class Settings {
  private var _email: String = _
  private var _password: String = _
  private var _proxyHost: String = _
  private var _proxyPort: String = _
  private var _scClientID: String = _
  private var _prefix: String = _
  private var _songQueueChannel: String = _

  def email: String = _email
  def email_=(e: String): Unit = _email = e

  def password: String = _password
  def password_=(p: String): Unit = _password = p

  def proxyHost: String = _proxyHost
  def proxyHost_=(p: String): Unit = _proxyHost = p

  def proxyPort: String = _proxyPort
  def proxyPort_=(p: String): Unit = _proxyPort = p

  def scClientID: String = _scClientID
  def scClientID_=(s: String): Unit = _scClientID = s

  def prefix: String = _prefix
  def prefix_=(p: String): Unit = _prefix = p

  def songQueueChannel: String = _songQueueChannel
  def songQueueChannel_=(s: String): Unit = _songQueueChannel = s
}
