package io.scalac.periscope.common

/**
Cross-compilation helper, abstracting over a specific queue implementation.
Allows to use a more performant ArrayDeque in Scala 2.13.
 */
trait Deque[A] {
  def toVector: Vector[A]
  def removeLast: A
  def prepend(a: A): Unit
  def size: Int
  def lastOption: Option[A]
  def count(f: A => Boolean): Int
}
