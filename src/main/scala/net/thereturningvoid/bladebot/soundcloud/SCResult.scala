package net.thereturningvoid.bladebot.soundcloud

object SCResult extends Enumeration {

  // All went well, successfully created object
  final val SUCCESS = Value(0)

  // Could not create a /resolve URL
  final val SC_RESOLVE_CREATE_FAIL = Value(1)
  // Could not connect to a /resolve URL
  final val SC_RESOLVE_CONNECT_FAIL = Value(2)
  // Invalid response code from a /resolve URL
  final val SC_RESOLVE_INVALID_RESPONSE_CODE = Value(3)

  // Track is not streamable
  final val SC_TRACK_NOT_STREAMABLE = Value(4)
  // Could not create a stream URL
  final val SC_STREAM_URL_CREATE_FAIL = Value(5)
  // Could not connect to a stream URL
  final val SC_STREAM_URL_CONNECT_FAIL = Value(6)

  // Queue already contains song
  final val QUEUE_CONTAINS_SONG = Value(7)
  // Queue does not contain song
  final val QUEUE_SONG_NOT_FOUND = Value(8)

}
