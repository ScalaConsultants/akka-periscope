package io.scalac.periscope.akka.tree

final case class ActorTree(nodes: Map[String, ActorTree]) extends AnyVal {

  /**
   * Simple no-dependency JSON serializer, that builds actor tree as nested json objects.
   * For example, for actor hierarchy of a -> b -> c (a is the root), you'll get following json:
   * {{{
   * {
   *   "a": {
   *     "b": {
   *       "c": {}
   *     }
   *   }
   * }
   * }}}
   *
   * No escaping is needed due to actor naming restrictions.
   */
  def asJson: String =
    if (nodes.isEmpty) "{}"
    else
      nodes
        .foldLeft("{") {
          case (agg, (actor, children)) =>
            s"""$agg"$actor":${children.asJson},"""
        }
        .init + "}"
}

object ActorTree {
  val empty: ActorTree = ActorTree(Map.empty)
}
