package net.thereturningvoid.bladebot.soundcloud

import play.api.libs.json._

class SCUser(json: JsObject) {

  lazy val id = (json \ "id").asOpt[Int]
  lazy val permalink = (json \ "permalink").asOpt[String]
  lazy val username = (json \ "username").asOpt[String]
  lazy val uri = (json \ "uri").asOpt[String]
  lazy val permalinkUrl = (json \ "permalink_url").asOpt[String]
  lazy val avatarUrl = (json \ "avatar_url").asOpt[String]
  lazy val country = (json \ "country").asOpt[String]
  lazy val fullName = (json \ "full_name").asOpt[String]
  lazy val city = (json \ "city").asOpt[String]
  lazy val description = (json \ "description").asOpt[String]
  lazy val disgocsName = (json \ "discogs-name").asOpt[String]
  lazy val myspaceName = (json \ "myspace-name").asOpt[String] // lol, myspace
  lazy val website = (json \ "website").asOpt[String]
  lazy val websiteTitle = (json \ "website-title").asOpt[String]
  lazy val online = (json \ "online").asOpt[Boolean]
  lazy val trackCount = (json \ "track_count").asOpt[Int]
  lazy val playlistCount = (json \ "playlist_count").asOpt[Int]
  lazy val followersCount = (json \ "followers_count").asOpt[Int]
  lazy val followingsCount = (json \ "followings_count").asOpt[Int]
  lazy val publicFavouritesCount = (json \ "public_favourites_count").asOpt[Int]

}
