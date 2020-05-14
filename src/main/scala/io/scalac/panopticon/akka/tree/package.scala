package io.scalac.panopticon.akka

import java.time.Instant

import akka.actor.{ ActorSystem, Props }
import akka.util.Timeout
import akka.pattern.{ after, ask }

import scala.concurrent.{ ExecutionContext, Future }

package object tree {

  import io.scalac.panopticon.akka.tree.ActorTreeBuilder._

  def build(system: ActorSystem)(implicit ec: ExecutionContext, timeout: Timeout): Future[ActorTree] = {
    val correlation = Instant.now().toEpochMilli
    val builder = system.actorOf(
      Props[ActorTreeBuilder](new ActorTreeBuilder(correlation)),
      s"actor-tree-builder-$correlation"
    )
    builder ! Build
    after(timeout.duration, system.scheduler)((builder ? GetTree).mapTo[ActorTree])
  }
}
