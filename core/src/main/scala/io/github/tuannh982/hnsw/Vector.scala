package io.github.tuannh982.hnsw

case class Vector[T](arr: Array[T]) {
  def dimension: Int = arr.length
}
