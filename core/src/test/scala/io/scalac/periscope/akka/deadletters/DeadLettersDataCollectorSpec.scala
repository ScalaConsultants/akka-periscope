package io.scalac.periscope.akka.deadletters

import akka.actor.{ Actor, ActorSystem, PoisonPill, Props }
import akka.dispatch.{ BoundedMessageQueueSemantics, RequiresMessageQueue }
import akka.testkit.{ ImplicitSender, TestKit }
import io.scalac.periscope.akka._
import org.scalatest.{ BeforeAndAfterAll, Inside }
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.{ Milliseconds, Span }
import akka.pattern.ask
import akka.util.Timeout
import io.scalac.periscope.akka.deadletters.DeadLettersDataCollector._

import scala.concurrent.duration._

class DeadLettersDataCollectorSpec
    extends TestKit(ActorSystem("DeadLettersDataCollectorSpec"))
    with Matchers
    with ImplicitSender
    with AnyFlatSpecLike
    with ScalaFutures
    with Inside
    with BeforeAndAfterAll {

  override implicit def patienceConfig: PatienceConfig = PatienceConfig(scaled(Span(600, Milliseconds)))
  private implicit val timeout: Timeout                = Timeout(1.second)

  private def defaultWaitSpan = scaled(Span(2000, Milliseconds))
  private def waitForEventBusToPropagateDeadLetters() = Thread.sleep(defaultWaitSpan.toMillis)

  "DeadLettersDataCollector" should "collect dead letters" in {
    val a         = system.actorOf(Props(new ActorA), "a1")
    val collector = system.actorOf(Props(new DeadLettersDataCollector(10)), "collector1")

    a ! KnownMessage("alive")
    a ! PoisonPill
    a ! KnownMessage("dead")
    waitForEventBusToPropagateDeadLetters()

    val snapshot = (collector ? GetSnapshot).mapTo[Snapshot].futureValue
    val window   = (collector ? CalculateForWindow(3000)).mapTo[WindowSnapshot].futureValue

    inside(snapshot.deadLetters) {
      case Vector(dead) => dead.value.message shouldBe KnownMessage("dead")
    }
    snapshot.unhandled should be(empty)
    snapshot.dropped should be(empty)

    window.deadLetters.count shouldBe 1
    window.unhandled.count shouldBe 0
    window.dropped.count shouldBe 0
    system.stop(collector)
  }

  it should "collect unhandled messages" in {
    val a         = system.actorOf(Props(new ActorA), "a2")
    val collector = system.actorOf(Props(new DeadLettersDataCollector(10)), "collector2")

    a ! KnownMessage("alive")
    a ! UnknownMessage("am I?")
    a ! UnknownMessage("Something is wrong")
    a ! UnknownMessage("Luke, I'm your father!")
    waitForEventBusToPropagateDeadLetters()

    val snapshot = (collector ? GetSnapshot).mapTo[Snapshot].futureValue
    val window   = (collector ? CalculateForWindow(3000)).mapTo[WindowSnapshot].futureValue

    inside(snapshot.unhandled) {
      case Vector(m1, m2, m3) =>
        // reverse order - latest first
        m1.value.message shouldBe UnknownMessage("Luke, I'm your father!")
        m2.value.message shouldBe UnknownMessage("Something is wrong")
        m3.value.message shouldBe UnknownMessage("am I?")
    }
    snapshot.deadLetters should be(empty)
    snapshot.dropped should be(empty)

    window.deadLetters.count shouldBe 0
    window.unhandled.count shouldBe 3
    window.dropped.count shouldBe 0
    system.stop(collector)
  }

  it should "not keep more messages than required" in {
    val a         = system.actorOf(Props(new ActorA), "a3")
    val collector = system.actorOf(Props(new DeadLettersDataCollector(5)), "collector3")

    a ! UnknownMessage("1")
    a ! UnknownMessage("2")
    a ! UnknownMessage("3")
    a ! UnknownMessage("4")
    a ! UnknownMessage("5")
    a ! UnknownMessage("6")
    a ! UnknownMessage("7")
    waitForEventBusToPropagateDeadLetters()

    val snapshot = (collector ? GetSnapshot).mapTo[Snapshot].futureValue
    val window   = (collector ? CalculateForWindow(3000)).mapTo[WindowSnapshot].futureValue

    snapshot.unhandled.map(_.value.message.asInstanceOf[UnknownMessage].text) shouldBe Vector("7", "6", "5", "4", "3")
    snapshot.deadLetters should be(empty)
    snapshot.dropped should be(empty)

    window.deadLetters.count shouldBe 0
    window.unhandled.count shouldBe 5
    window.dropped.count shouldBe 0
    system.stop(collector)
  }

  it should "mark window calculations as estimates if window is not full" in {
    val a         = system.actorOf(Props(new ActorA), "a4")
    val collector = system.actorOf(Props(new DeadLettersDataCollector(10)), "collector4")

    a ! UnknownMessage("1")
    a ! UnknownMessage("2")
    a ! UnknownMessage("3")
    waitForEventBusToPropagateDeadLetters()

    val window = (collector ? CalculateForWindow(3000)).mapTo[WindowSnapshot].futureValue

    window.unhandled.count shouldBe 3
    window.unhandled.isMinimumEstimate shouldBe true
    system.stop(collector)
  }

//  it should "mark window calculations as precise if window is fully cached" in {
//    val a         = system.actorOf(Props(new ActorA), "a5")
//    val collector = system.actorOf(Props(new DeadLettersDataCollector(10)), "collector5")
//
//    a ! UnknownMessage("1")
//    waitForEventBusToPropagateDeadLetters()
//    a ! UnknownMessage("2")
//    waitForEventBusToPropagateDeadLetters()
//    a ! UnknownMessage("3")
//    waitForEventBusToPropagateDeadLetters()
//
//    val window = (collector ? CalculateForWindow(defaultWaitSpan.toMillis)).mapTo[WindowSnapshot].futureValue
//
//    window.unhandled.count should be > 0
//    window.unhandled.isMinimumEstimate shouldBe false
//    system.stop(collector)
//  }

  override def afterAll: Unit =
    TestKit.shutdownActorSystem(system)

}

class BoundedActor extends Actor with RequiresMessageQueue[BoundedMessageQueueSemantics] {
  def receive: Receive = {
    case _ =>
      Thread.sleep(3000)
  }
}
