package io.scalac.periscope.akka.http

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.http.scaladsl.model.ContentTypes.`application/json`
import akka.http.scaladsl.model.{ HttpEntity, HttpResponse, StatusCodes }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import io.scalac.periscope.akka.tree.build

import scala.concurrent.{ ExecutionContext, Future }

object ActorTreeRoute {

  private def buildFor(system: ActorSystem, timeoutMs: Long)(implicit ec: ExecutionContext): Future[HttpResponse] = {
    implicit val timeout: Timeout = Timeout(timeoutMs, TimeUnit.MILLISECONDS)
    build(system).map(tree =>
      HttpResponse(
        StatusCodes.OK,
        entity = HttpEntity(tree.asJson).withContentType(`application/json`)
      )
    )
  }

  def apply(system: ActorSystem)(implicit ec: ExecutionContext): Route = get {
    parameters("timeout".as[Long])(timeoutMs => complete(buildFor(system, timeoutMs)))
  }

  def apply(systems: Map[String, ActorSystem])(implicit ec: ExecutionContext): Route = get {
    parameters("timeout".as[Long], "system".as[String])((timeoutMs, system) =>
      systems.get(system) match {
        case Some(s) => complete(buildFor(s, timeoutMs))
        case None    => complete(StatusCodes.NotFound)
      }
    )
  }
}
