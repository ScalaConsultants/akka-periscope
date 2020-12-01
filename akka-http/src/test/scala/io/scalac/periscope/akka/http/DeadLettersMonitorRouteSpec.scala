package io.scalac.periscope.akka.http

import akka.actor.{ PoisonPill, Props }
import akka.http.scaladsl.model.{ ContentTypes, StatusCodes }
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.testkit.TestKit
import io.circe.Json
import io.circe.parser._
import io.scalac.periscope.akka.{ ActorA, KnownMessage, UnknownMessage }
import org.scalatest.{ BeforeAndAfterAll, EitherValues }
import org.scalatest.concurrent.{ Eventually, ScalaFutures }
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class DeadLettersMonitorRouteSpec
    extends AnyFlatSpec
    with Matchers
    with EitherValues
    with ScalaFutures
    with Eventually
    with ScalatestRouteTest
    with BeforeAndAfterAll {

  "DeadLettersMonitorRoute" should "return deadletters monitoring information" in {
    val a = system.actorOf(Props(new ActorA), "a1")

    val route = DeadLettersMonitorRoute(keepMax = 5)

    Get("/?window=300") ~> route ~> check {
      status shouldEqual StatusCodes.OK
      contentType shouldEqual ContentTypes.`application/json`
      parse(responseAs[String])
        .getOrElse(fail("Invalid json")) shouldEqual parse(
        """
          |{
          |   "snapshot":{
          |      "deadLetters":[],
          |      "unhandled":[],
          |      "dropped":[]
          |   },
          |   "window":{
          |      "withinMillis":300,
          |      "deadLetters":{
          |         "count":0,
          |         "isMinimumEstimate":false
          |      },
          |      "unhandled":{
          |         "count":0,
          |         "isMinimumEstimate":false
          |      },
          |      "dropped":{
          |         "count":0,
          |         "isMinimumEstimate":false
          |      }
          |   }
          |}
          """.stripMargin
      ).getOrElse(fail("Invalid json"))
    }

    a ! KnownMessage("alive1")
    a ! UnknownMessage("1")
    a ! UnknownMessage("2")
    a ! UnknownMessage("3")
    a ! UnknownMessage("4")
    a ! KnownMessage("alive2")
    a ! UnknownMessage("5")
    a ! UnknownMessage("6")
    a ! UnknownMessage("7")
    a ! PoisonPill
    a ! KnownMessage("dead1")
    a ! KnownMessage("dead2")

    eventually(
      Get("/?window=5000") ~> route ~> check {
        status shouldEqual StatusCodes.OK
        contentType shouldEqual ContentTypes.`application/json`
        val json = parse(responseAs[String]).getOrElse(fail("Invalid json"))

        val deadLetters1 = json.hcursor.downField("snapshot").downField("deadLetters").downArray
        deadLetters1.downField("value").downField("message").as[String] shouldBe Right("KnownMessage(dead2)")
        deadLetters1.downField("timestamp").as[Long].getOrElse(0L) should be > 0L

        val deadLetters2 = json.hcursor.downField("snapshot").downField("deadLetters").downN(1)
        deadLetters2.downField("value").downField("message").as[String] shouldBe Right("KnownMessage(dead1)")
        deadLetters2.downField("timestamp").as[Long].getOrElse(0L) should be > 0L

        val unhandled = json.hcursor.downField("snapshot").downField("unhandled")
        unhandled.downArray.downField("value").downField("message").as[String] shouldBe Right("UnknownMessage(7)")
        unhandled.downN(1).downField("value").downField("message").as[String] shouldBe Right("UnknownMessage(6)")
        unhandled.downN(4).downField("value").downField("message").as[String] shouldBe Right("UnknownMessage(3)")
        unhandled.downN(5).as[Json].isLeft shouldBe true

        json.hcursor.downField("window").as[Json] shouldBe parse(
          """{
            |      "withinMillis":5000,
            |      "deadLetters":{
            |         "count":2,
            |         "isMinimumEstimate":true
            |      },
            |      "unhandled":{
            |         "count":5,
            |         "isMinimumEstimate":true
            |      },
            |      "dropped":{
            |         "count":0,
            |         "isMinimumEstimate":false
            |      }
            |   }""".stripMargin
        )
      }
    )
  }

  it should "properly escape json" in {
    val a = system.actorOf(Props(new ActorA), "a2")

    val route = DeadLettersMonitorRoute(keepMax = 5, name = "DeadLettersMonitor1")

    a ! UnknownMessage("\"this\\\"is a \"\\\" tough message")

    eventually(
      Get("/?window=5000") ~> route ~> check {
        status shouldEqual StatusCodes.OK
        contentType shouldEqual ContentTypes.`application/json`
        val json = parse(responseAs[String]).getOrElse(fail("Invalid json"))

        val unhandled = json.hcursor.downField("snapshot").downField("unhandled")
        unhandled.downArray.downField("value").downField("message").as[String] shouldBe Right(
          "UnknownMessage(\"this\\\"is a \"\\\" tough message)"
        )
      }
    )
  }
  override def afterAll: Unit =
    TestKit.shutdownActorSystem(system)
}
