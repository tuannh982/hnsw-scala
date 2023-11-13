package io.github.tuannh982.hnsw

import scala.util.hashing.MurmurHash3

case class Vec[T](arr: Array[T]) {
  def dimension: Int = arr.length

  override def hashCode(): Int = MurmurHash3.arrayHash(arr)

  override def toString: String = arr.mkString("Array(", ", ", ")")
}

object Vec {

  def equals[T](u: Vec[T], v: Vec[T])(implicit ord: Ordering[T]): Boolean = {
    if (u.dimension == v.dimension) {
      for (i <- u.arr.indices) {
        if (u.arr(i) != v.arr(i)) {
          return false
        }
      }
      true
    } else {
      false
    }
  }

  def ordering[T]()(implicit ord: Ordering[T]): Ordering[Vec[T]] = new Ordering[Vec[T]] {

    override def compare(u: Vec[T], v: Vec[T]): Int = {
      Utils.assert(u.dimension == v.dimension, s"u dim=${u.dimension}, v dim=${v.dimension}")
      for (i <- u.arr.indices) {
        val cmp = ord.compare(u.arr(i), v.arr(i))
        if (cmp != 0) {
          return cmp
        }
      }
      0
    }
  }
}
