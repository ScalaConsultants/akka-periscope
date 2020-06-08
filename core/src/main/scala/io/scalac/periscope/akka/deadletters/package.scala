package io.scalac.periscope.akka

import akka.actor.{ ActorRef, ActorSystem, DeadLetter, Dropped, UnhandledMessage }

package object deadletters {

  private[akka] def subscribe(system: ActorSystem, actor: ActorRef): Unit = {
    system.eventStream.subscribe(actor, classOf[DeadLetter])
    system.eventStream.subscribe(actor, classOf[UnhandledMessage])
    system.eventStream.subscribe(actor, classOf[Dropped])
  }

  private[akka] def unsubscribe(system: ActorSystem, actor: ActorRef): Unit =
    system.eventStream.unsubscribe(actor)
}
