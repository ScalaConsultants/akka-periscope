package io.scalac.periscope.common

trait Deque[A] {
  def toVector: Vector[A]
  def removeLast: A
  def prepend(a: A): Unit
  def size: Int
  def lastOption: Option[A]
  def count(f: A => Boolean): Int
}
