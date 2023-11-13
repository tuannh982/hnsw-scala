package io.github.tuannh982.hnsw

trait Knn[T, D] {
  val dimension: Int
  val df: DistanceFunction[T, D]
  val distanceOrd: Ordering[D]

  def add(vector: Vector[T]): Unit

  def knn(vector: Vector[T], k: Int): Seq[Vector[T]]
}