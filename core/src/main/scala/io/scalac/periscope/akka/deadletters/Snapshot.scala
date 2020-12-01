package io.scalac.periscope.akka.deadletters

import akka.actor.{ DeadLetter, Dropped, UnhandledMessage }
import io.scalac.periscope.common._
import Snapshot._

final case class Snapshot(
  deadLetters: Vector[Timestamped[DeadLetter]],
  unhandled: Vector[Timestamped[UnhandledMessage]],
  dropped: Vector[Timestamped[Dropped]]
) {
  def toJson: String =
    s"""{"deadLetters":${vectorToJson(deadLetters, deadLetterToJson)},"unhandled":${vectorToJson(
      unhandled,
      unhandledToJson
    )},"dropped":${vectorToJson(
      dropped,
      droppedToJson
    )}}"""
}

object Snapshot {
  private def vectorToJson[A](v: Vector[Timestamped[A]], atoj: A => String) =
    if (v.isEmpty) "[]"
    else
      v.foldLeft("[") { case (agg, i) =>
        s"""$agg{"timestamp":${i.timestamp},"value":${atoj(i.value)}},"""
      }.init + "]"

  private def deadLetterToJson(a: DeadLetter) =
    s"""{"message":"${json.escape(a.message.toString)}","sender":"${a.sender.toString()}","recipient":"${a.recipient
      .toString()}"}"""

  private def unhandledToJson(a: UnhandledMessage) =
    s"""{"message":"${json.escape(a.message.toString)}","sender":"${a.sender.toString()}","recipient":"${a.recipient
      .toString()}"}"""

  private def droppedToJson(a: Dropped) =
    s"""{"message":"${json.escape(a.message.toString)}","sender":"${a.sender.toString()}","recipient":"${a.recipient
      .toString()}","reason":"${json.escape(a.reason)}"}"""
}
