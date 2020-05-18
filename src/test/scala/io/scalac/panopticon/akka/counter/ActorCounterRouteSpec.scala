package io.scalac.panopticon.akka.counter

import akka.actor.Props
import akka.http.scaladsl.model.{ ContentTypes, StatusCodes }
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.testkit.TestKit
import io.scalac.panopticon.akka.ActorA
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ActorCounterRouteSpec
    extends AnyFlatSpec
    with Matchers
    with ScalaFutures
    with ScalatestRouteTest
    with BeforeAndAfterAll {

  "ActorCounterRoute" should "return actor count json" in {
    system.actorOf(Props(new ActorA), "a")
    val route = ActorCountRoute(system)
    Get("/?timeout=300") ~> route ~> check {
      status shouldEqual StatusCodes.OK
      contentType shouldEqual ContentTypes.`application/json`
      responseAs[String] shouldEqual """{"result":14}"""
    }
  }

  it should "respond with 404 if no timeout parameter was specified" in {
    val route = Route.seal(ActorCountRoute(system))
    Get("/") ~> route ~> check {
      status shouldEqual StatusCodes.NotFound
    }
  }

  override def afterAll: Unit =
    TestKit.shutdownActorSystem(system)
}
