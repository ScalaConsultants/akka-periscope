package io.scalac.periscope.akka.tree

import akka.actor.{ ActorSystem, Props }
import akka.testkit.{ ImplicitSender, TestKit }
import akka.util.Timeout
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.{ Milliseconds, Span }
import io.scalac.periscope.akka._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class ActorTreeBuilderSpec
    extends TestKit(ActorSystem("ActorTreeBuilderSpec"))
    with Matchers
    with ImplicitSender
    with AnyFlatSpecLike
    with ScalaFutures
    with BeforeAndAfterAll {

  override implicit def patienceConfig: PatienceConfig = PatienceConfig(scaled(Span(600, Milliseconds)))

  "ActorTreeBuilder" should "build the whole tree if all the actors are responsive" in {
    system.actorOf(Props(new ActorA), "a")
    implicit val timeout: Timeout = Timeout(300.millis)
    val tree                      = build(system).futureValue
    tree.nodes("user").nodes("a").nodes("ab").nodes shouldBe Map("abc1" -> ActorTree.empty, "abc2" -> ActorTree.empty)
    tree.nodes("user").nodes("a").nodes("ac").nodes shouldBe Map.empty
  }

  it should "provide bounded execution time by not including actors that can't respond in time" in {
    system.actorOf(Props(new OverwhelmedActorParent), "o")
    implicit val timeout: Timeout = Timeout(300.millis)
    val tree                      = build(system).futureValue
    tree.nodes("user").nodes("o").nodes shouldBe Map("oc1" -> ActorTree.empty, "oc2" -> ActorTree.empty)
  }

  override def afterAll: Unit =
    TestKit.shutdownActorSystem(system)

}
