package io.scalac.periscope.akka.http

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.http.scaladsl.model.ContentTypes.`application/json`
import akka.http.scaladsl.model.{ HttpEntity, HttpResponse, StatusCodes }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import io.scalac.periscope.akka.counter.count

import scala.concurrent.{ ExecutionContext, Future }

object ActorCountRoute {

  private def countFor(system: ActorSystem, timeoutMs: Long)(implicit ec: ExecutionContext): Future[HttpResponse] = {
    implicit val timeout: Timeout = Timeout(timeoutMs, TimeUnit.MILLISECONDS)
    count(system).map(res =>
      HttpResponse(
        StatusCodes.OK,
        entity = HttpEntity(s"""{"result":$res}""").withContentType(`application/json`)
      )
    )
  }

  def apply(system: ActorSystem)(implicit ec: ExecutionContext): Route = get {
    parameters("timeout".as[Long])(timeoutMs => complete(countFor(system, timeoutMs)))
  }

  def apply(systems: Map[String, ActorSystem])(implicit ec: ExecutionContext): Route = get {
    parameters("timeout".as[Long], "system".as[String])((timeoutMs, system) =>
      systems.get(system) match {
        case Some(s) => complete(countFor(s, timeoutMs))
        case None    => complete(StatusCodes.NotFound)
      }
    )
  }
}
