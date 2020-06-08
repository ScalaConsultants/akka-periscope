package io.scalac.periscope.common

object json {

  def escape(s: String): String =
    s.replace("\\", "\\\\").replace("\"", "\\\"")
}
