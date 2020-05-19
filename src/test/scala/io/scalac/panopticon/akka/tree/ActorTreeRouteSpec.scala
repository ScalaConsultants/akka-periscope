package io.scalac.panopticon.akka.tree

import akka.actor.Props
import akka.http.scaladsl.model.{ ContentTypes, StatusCodes }
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.testkit.TestKit
import io.circe.parser._
import io.scalac.panopticon.akka.ActorA
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ActorTreeRouteSpec
    extends AnyFlatSpec
    with Matchers
    with ScalaFutures
    with ScalatestRouteTest
    with BeforeAndAfterAll {

  "ActorTreeRoute" should "return actor tree json" in {
    system.actorOf(Props(new ActorA), "a")
    val route = ActorTreeRoute(system)
    Get("/?timeout=300") ~> route ~> check {
      status shouldEqual StatusCodes.OK
      contentType shouldEqual ContentTypes.`application/json`
      parse(responseAs[String])
        .getOrElse(fail("Invalid json"))
        .asObject
        .get("user")
        .get
        .asObject
        .get("a")
        .get shouldEqual parse("""{"ab":{"abc1":{},"abc2":{}},"ac":{}}""").getOrElse(fail("Invalid json"))
    }
  }

  it should "respond with 404 if no timeout parameter was specified" in {
    val route = Route.seal(ActorTreeRoute(system))
    Get("/") ~> route ~> check {
      status shouldEqual StatusCodes.NotFound
    }
  }

  override def afterAll: Unit =
    TestKit.shutdownActorSystem(system)
}
