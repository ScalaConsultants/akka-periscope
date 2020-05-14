package io.scalac.panopticon.akka.tree

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ActorTreeSpec extends AnyFlatSpec with Matchers {

  "ActorTree" should "serialize to proper json" in {

    val tree = ActorTree(
      Map(
        "user" -> ActorTree(Map("a" -> ActorTree(Map("b" -> ActorTree(Map("c" -> ActorTree(Map.empty))))))),
        "system" -> ActorTree(
          Map(
            "a" -> ActorTree(Map()),
            "b" -> ActorTree(Map())
          )
        )
      )
    )
    tree.asJson shouldEqual """{"user":{"a":{"b":{"c":{}}}},"system":{"a":{},"b":{}}}"""
  }

}
