package io.scalac.panopticon.akka.tree

import akka.actor.ActorPath

import scala.collection.mutable

private[tree] final class MutableActorTree(val nodes: mutable.Map[String, MutableActorTree]) extends AnyVal {
  def insert(p: ActorPath): MutableActorTree =
    p.elements.foldLeft(this) {
      case (n, segment) =>
        n.nodes.getOrElseUpdate(segment, new MutableActorTree(mutable.Map.empty))
    }

  def toActorTree: ActorTree = ActorTree(nodes.view.mapValues(_.toActorTree).toMap)
}
