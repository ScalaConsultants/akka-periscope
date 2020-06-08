package io.scalac.periscope.akka.deadletters

import akka.actor.{ DeadLetter, Dropped, UnhandledMessage }

final case class Snapshot(
  deadLetters: Vector[Timestamped[DeadLetter]],
  unhandled: Vector[Timestamped[UnhandledMessage]],
  dropped: Vector[Timestamped[Dropped]]
)

