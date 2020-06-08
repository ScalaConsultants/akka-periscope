package io.scalac.periscope.akka.deadletters

import akka.actor.{ DeadLetter, Dropped, UnhandledMessage }
import io.scalac.periscope.common.{ ArrayDeque, Deque }

private[deadletters] class DeadLettersDataCollector(keepMax: Int) extends AbstractDeadLettersDataCollector(keepMax) {

  override protected val lastDeadLetters: Deque[Timestamped[DeadLetter]] = ArrayDeque(
    new scala.collection.mutable.ArrayBuffer(keepMax)
  )
  override protected val lastUnhandled: Deque[Timestamped[UnhandledMessage]] = ArrayDeque(
    new scala.collection.mutable.ArrayBuffer(keepMax)
  )
  override protected val lastDropped: Deque[Timestamped[Dropped]] = ArrayDeque(
    new scala.collection.mutable.ArrayBuffer(keepMax)
  )
}
