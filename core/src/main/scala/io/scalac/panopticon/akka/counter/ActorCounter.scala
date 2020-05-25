package io.scalac.panopticon.akka.counter

import akka.actor.{ Actor, ActorIdentity, Identify }
import ActorCounter._

private[counter] class ActorCounter(correlation: Long) extends Actor {

  private var result: Long     = 0L;
  private var started: Boolean = false

  def receive: Receive = {
    case Count =>
      if (started)
        sender() ! AlreadyCounting
      else {
        started = true
        context.actorSelection("/*") ! Identify(correlation)
      }

    case GetResult =>
      sender() ! result

    case e: ActorIdentity if e.correlationId == correlation =>
      e.ref.foreach { r =>
        result += 1;
        context.actorSelection(r.path / "*") ! Identify(e.correlationId)
      }

  }
}

object ActorCounter {
  case object Count
  case object GetResult

  case object AlreadyCounting
}
