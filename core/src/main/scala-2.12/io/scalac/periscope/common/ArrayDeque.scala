package io.scalac.periscope.common

final case class ArrayDeque[A](underlying: scala.collection.mutable.ArrayBuffer[A]) extends Deque[A] {
  def toVector: Vector[A]         = underlying.toVector
  def removeLast: A               = underlying.remove(underlying.size - 1)
  def prepend(a: A): Unit         = underlying.prepend(a)
  def size: Int                   = underlying.size
  def lastOption: Option[A]       = underlying.lastOption
  def count(f: A => Boolean): Int = underlying.count(f)
}
