package io.scalac.periscope.akka

import java.time.Instant

import akka.actor.{ ActorSystem, Props }
import akka.pattern.{ after, ask }
import akka.util.Timeout

import scala.concurrent.{ ExecutionContext, Future }

package object counter {

  import io.scalac.periscope.akka.counter.ActorCounter._

  def count(system: ActorSystem)(implicit ec: ExecutionContext, timeout: Timeout): Future[Long] = {
    val correlation = Instant.now().toEpochMilli
    val counter     = system.actorOf(
      Props(new ActorCounter(correlation)),
      s"actor-counter-$correlation"
    )
    counter ! Count
    val result      = after(timeout.duration, system.scheduler)((counter ? GetResult).mapTo[Long])
    result.onComplete(_ => system.stop(counter))
    result
  }

}
