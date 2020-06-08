package io.scalac.periscope.akka.deadletters

import java.time.Instant

import akka.actor.{ Actor, DeadLetter, Dropped, UnhandledMessage }
import io.scalac.periscope.akka.deadletters.AbstractDeadLettersDataCollector._
import io.scalac.periscope.common.Deque

private[deadletters] abstract class AbstractDeadLettersDataCollector(keepMax: Int) extends Actor {

  private val eventStream = context.system.eventStream

  protected def lastDeadLetters: Deque[Timestamped[DeadLetter]]
  protected def lastUnhandled: Deque[Timestamped[UnhandledMessage]]
  protected def lastDropped: Deque[Timestamped[Dropped]]

  override def preStart(): Unit = {
    super.preStart()
    eventStream.subscribe(self, classOf[DeadLetter])
    eventStream.subscribe(self, classOf[UnhandledMessage])
    eventStream.subscribe(self, classOf[Dropped])
  }

  override def postStop(): Unit =
    eventStream.unsubscribe(self)

  def receive: Receive = {
    case m: DeadLetter       => enqueueAndKeepSize(lastDeadLetters, m)
    case m: UnhandledMessage => enqueueAndKeepSize(lastUnhandled, m)
    case m: Dropped          => enqueueAndKeepSize(lastDropped, m)

    case GetSnapshot => sender() ! Snapshot(lastDeadLetters.toVector, lastUnhandled.toVector, lastDropped.toVector)

    case CalculateForWindow(withinMillis) =>
      val from = Instant.now.toEpochMilli - withinMillis
      sender() ! WindowSnapshot(
        withinMillis,
        windowData(lastDeadLetters, from),
        windowData(lastUnhandled, from),
        windowData(lastDropped, from)
      )

  }

  private def enqueueAndKeepSize[A](queue: Deque[Timestamped[A]], message: A) = {
    if (queue.size == keepMax) {
      queue.removeLast
    }
    queue.prepend(Timestamped(message, Instant.now.toEpochMilli))
  }

  private def windowData[A](queue: Deque[Timestamped[A]], from: Long): WindowData = {
    val isFullWindow = containsWholeWindow(queue, from)
    val count        = countWindow(queue, from)
    WindowData(count, !isFullWindow)
  }

  private def containsWholeWindow[A](queue: Deque[Timestamped[A]], from: Long) =
    queue.lastOption.fold(true)(_.timestamp <= from)

  private def countWindow[A](queue: Deque[Timestamped[A]], from: Long): Int =
    queue.count(_.timestamp >= from)
}

object AbstractDeadLettersDataCollector {

  final case object GetSnapshot
  final case class Snapshot(
    deadLetters: Vector[Timestamped[DeadLetter]],
    unhandled: Vector[Timestamped[UnhandledMessage]],
    dropped: Vector[Timestamped[Dropped]]
  )

  final case class CalculateForWindow(withinMillis: Long)

  final case class WindowSnapshot(
    withinMillis: Long,
    deadLetters: WindowData,
    unhandled: WindowData,
    dropped: WindowData
  )

  final case class WindowData(count: Int, isMinimumEstimate: Boolean)

}
