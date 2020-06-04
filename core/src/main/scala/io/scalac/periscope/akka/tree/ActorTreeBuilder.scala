package io.scalac.periscope.akka.tree

import akka.actor.{ Actor, ActorIdentity, Identify }
import io.scalac.periscope.akka.tree.ActorTreeBuilder.{ AlreadyBuilding, Build, GetTree }

import scala.collection.mutable

private[tree] class ActorTreeBuilder(correlation: Long) extends Actor {

  private val tree: MutableActorTree = new MutableActorTree(mutable.Map.empty)
  private var started: Boolean       = false

  def receive: Receive = {
    case Build =>
      if (started)
        sender() ! AlreadyBuilding
      else {
        started = true
        context.actorSelection("/*") ! Identify(correlation)
      }

    case GetTree =>
      sender() ! tree.toActorTree

    case e: ActorIdentity if e.correlationId == correlation =>
      e.ref.foreach { r =>
        tree.insert(r.path)
        context.actorSelection(r.path / "*") ! Identify(e.correlationId)
      }

  }
}

private[tree] object ActorTreeBuilder {
  case object Build
  case object GetTree

  case object AlreadyBuilding
}
