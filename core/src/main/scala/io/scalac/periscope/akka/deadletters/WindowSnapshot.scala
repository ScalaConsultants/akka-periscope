package io.scalac.periscope.akka.deadletters

final case class WindowSnapshot(
  withinMillis: Long,
  deadLetters: WindowData,
  unhandled: WindowData,
  dropped: WindowData
){
  def toJson = s"""{"withinMillis":$withinMillis,"deadLetters":${deadLetters.toJson},"unhandled":${unhandled.toJson},"dropped":${dropped.toJson}}"""
}

final case class WindowData(count: Int, isMinimumEstimate: Boolean) {
  def toJson = s"""{"count":$count,"isMinimumEstimate":$isMinimumEstimate}"""
}
