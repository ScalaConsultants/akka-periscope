package io.scalac.periscope.akka.deadletters

final case class WindowSnapshot(
  withinMillis: Long,
  deadLetters: WindowData,
  unhandled: WindowData,
  dropped: WindowData
)

final case class WindowData(count: Int, isMinimumEstimate: Boolean)
