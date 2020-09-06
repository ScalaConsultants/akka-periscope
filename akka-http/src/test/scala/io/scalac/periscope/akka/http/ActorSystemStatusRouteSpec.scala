package io.scalac.periscope.akka.http

import akka.actor.Props
import akka.http.scaladsl.model.{ ContentTypes, StatusCodes }
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.testkit.TestKit
import io.scalac.periscope.akka.ActorA
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import io.circe.parser._

class ActorSystemStatusRouteSpec
    extends AnyFlatSpec
    with Matchers
    with ScalaFutures
    with ScalatestRouteTest
    with BeforeAndAfterAll {

  "ActorCounterRoute" should "return actor count json" in {
    system.actorOf(Props(new ActorA), "a")
    val route = ActorSystemStatusRoute(system)
    Get("/?timeout=300") ~> route ~> check {
      status shouldEqual StatusCodes.OK
      contentType shouldEqual ContentTypes.`application/json`
      val respBody = parse(responseAs[String]).getOrElse(fail("Invalid json")).hcursor
      respBody.downField("actorCount").as[Long] shouldBe Right(13L)
      respBody.downField("startTime").as[Long].isRight shouldBe true
      respBody.downField("uptime").as[Long].isRight shouldBe true
    }
  }

  it should "respond with 404 if no timeout parameter was specified" in {
    val route = Route.seal(ActorSystemStatusRoute(system))
    Get("/") ~> route ~> check {
      status shouldEqual StatusCodes.NotFound
    }
  }

  override def afterAll: Unit =
    TestKit.shutdownActorSystem(system)
}
