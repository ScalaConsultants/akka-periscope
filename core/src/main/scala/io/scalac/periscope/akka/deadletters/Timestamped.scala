package io.scalac.periscope.akka.deadletters

final case class Timestamped[A](value: A, timestamp: Long)
