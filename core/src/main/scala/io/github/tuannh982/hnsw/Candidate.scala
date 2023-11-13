package io.github.tuannh982.hnsw

case class Candidate[D](index: Int, distance: D)

object Candidate {

  def fromDistanceOrd[D](ord: Ordering[D]): Ordering[Candidate[D]] =
    (x: Candidate[D], y: Candidate[D]) => ord.compare(x.distance, y.distance)
}
