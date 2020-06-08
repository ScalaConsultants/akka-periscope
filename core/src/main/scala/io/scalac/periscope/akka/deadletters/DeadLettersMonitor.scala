package io.scalac.periscope.akka.deadletters

import akka.actor.{ ActorSystem, Props }
import akka.pattern.ask
import akka.util.Timeout
import io.scalac.periscope.akka.deadletters.AbstractDeadLettersDataCollector._

import scala.concurrent.Future

trait DeadLettersMonitor {
  def snapshot: Future[Snapshot]
  def window(withinMillis: Long): Future[WindowSnapshot]
  def shutdown: Future[Unit]
}

object DeadLettersMonitor {
  def start(keepMax: Int, name: String = "DeadLettersMonitor")(
    implicit system: ActorSystem,
    timeout: Timeout
  ): DeadLettersMonitor = {
    val collector = system.actorOf(Props(new DeadLettersDataCollector(keepMax)), name)

    subscribe(system, collector)

    new DeadLettersMonitor {
      def snapshot: Future[Snapshot] = (collector ? GetSnapshot).mapTo[Snapshot]

      def window(withinMillis: Long): Future[WindowSnapshot] =
        (collector ? CalculateForWindow(withinMillis)).mapTo[WindowSnapshot]

      def shutdown: Future[Unit] =
        Future(unsubscribe(system, collector))(system.dispatcher)
    }
  }
}
