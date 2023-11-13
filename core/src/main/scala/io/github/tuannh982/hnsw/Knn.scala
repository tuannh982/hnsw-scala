package io.github.tuannh982.hnsw

trait Knn[T, D] {
  val dimension: Int
  val df: DistanceFunction[T, D]
  val distanceOrd: Ordering[D]

  def add(vector: Vec[T]): Unit

  def knn(vector: Vec[T], k: Int): Seq[Vec[T]]
}