package io.scalac.panopticon.akka.counter

import akka.actor.{ ActorSystem, Props }
import akka.testkit.{ ImplicitSender, TestKit }
import akka.util.Timeout
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.{ Milliseconds, Span }
import io.scalac.panopticon.akka._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class ActorCounterSpec
    extends TestKit(ActorSystem("ActorCounterSpec"))
    with Matchers
    with ImplicitSender
    with AnyFlatSpecLike
    with ScalaFutures
    with BeforeAndAfterAll {

  override implicit def patienceConfig: PatienceConfig = PatienceConfig(scaled(Span(350, Milliseconds)))

  "ActorCounter" should "count all the actors that are responsive" in {
    system.actorOf(Props(new ActorA), "a")
    implicit val timeout: Timeout = Timeout(300.millis)
    count(system).futureValue shouldBe 12
  }

  it should "provide bounded execution time by not including actors that can't respond in time" in {
    system.actorOf(Props(new OverwhelmedActorParent), "o")
    implicit val timeout: Timeout = Timeout(300.millis)
    count(system).futureValue shouldBe 15
  }

  override def afterAll: Unit =
    TestKit.shutdownActorSystem(system)

}
