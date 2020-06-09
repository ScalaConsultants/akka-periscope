package io.scalac.periscope.akka.http

import akka.actor.ActorSystem
import akka.http.scaladsl.model.ContentTypes.`application/json`
import akka.http.scaladsl.model.{ HttpEntity, HttpResponse, StatusCodes }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import io.scalac.periscope.akka.deadletters.{ DeadLettersMonitor, Snapshot, WindowSnapshot }

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object DeadLettersMonitorRoute {

  def apply(
    keepMax: Int = 100,
    timeout: Timeout = Timeout(1.second),
    name: String = "DeadLettersMonitor"
  )(implicit system: ActorSystem, ec: ExecutionContext): Route = {

    implicit val t = timeout
    val monitor    = DeadLettersMonitor.start(keepMax, name)

    get {
      parameters("window".as[Long])(window =>
        complete {
          val wf = monitor.window(window)
          for {
            s <- monitor.snapshot
            w <- wf
          } yield HttpResponse(
            StatusCodes.OK,
            entity = HttpEntity(Response(s, w).toJson).withContentType(`application/json`)
          )
        }
      )
    }
  }

  final case class Response(
    snapshot: Snapshot,
    window: WindowSnapshot
  ) {
    def toJson: String = s"""{"snapshot":${snapshot.toJson}, "window":${window.toJson}}"""
  }
}
