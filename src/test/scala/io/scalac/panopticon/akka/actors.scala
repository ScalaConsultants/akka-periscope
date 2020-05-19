package io.scalac.panopticon.akka

import akka.actor.{ Actor, Props }

class ActorA extends Actor {
  def receive: Receive = PartialFunction.empty
  override def preStart(): Unit = {
    context.actorOf(Props(new ActorB), name = "ab")
    context.actorOf(Props(new ActorC), name = "ac")
    super.preStart()
  }
}

class ActorB extends Actor {
  def receive: Receive = PartialFunction.empty
  override def preStart(): Unit = {
    context.actorOf(Props(new ActorC), name = "abc1")
    context.actorOf(Props(new ActorC), name = "abc2")
    super.preStart()
  }
}

class ActorC extends Actor {
  def receive: Receive = PartialFunction.empty
}

class OverwhelmedActorParent extends Actor {
  def receive: Receive = PartialFunction.empty

  override def preStart(): Unit = {
    context.actorOf(Props(new OverwhelmedActor), name = "overwhelmed")
    context.actorOf(Props(new ActorC), name = "oc1")
    context.actorOf(Props(new ActorC), name = "oc2")
    super.preStart()
  }
}

class OverwhelmedActor extends Actor {
  def receive: Receive = {
    case () => Thread.sleep(5000)
  }
  override def preStart(): Unit =
    self.!(())
}
