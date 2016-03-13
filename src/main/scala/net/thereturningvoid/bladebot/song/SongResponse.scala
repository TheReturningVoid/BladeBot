package net.thereturningvoid.bladebot.song

object SongResponse extends Enumeration {
  // Soundcloud resolve errors
  val SC_RESOLVE_RESPONSE_NOT_302 = Value
  val SC_RESOLVE_IOEXCEPTION = Value
  val SC_RESOLVE_BAD_URL = Value

  // Soundcloud stream errors
  val SC_STREAM_CANNOT_CONNECT = Value
  val SC_STREAM_NOT_STREAMABLE = Value
  val SC_STREAM_MISSING_STREAM_URL = Value

  // Queue errors
  val QUEUE_CONTAINS_SONG = Value
  val QUEUE_DOES_NOT_CONTAIN_SONG = Value

}
