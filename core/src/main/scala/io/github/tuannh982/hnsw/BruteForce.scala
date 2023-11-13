package io.github.tuannh982.hnsw

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
  * Base class for brute-force KNN implementation, only used for benchmarking our HNSW
  * **note**: this implementation is not thread-safe
  *
  * @param dimension number of dimension of all vectors in this model
  * @param df the distance function
  * @param distanceOrd the distance ordering, used to compare distance
  */
abstract class BruteForce[T, D](
  override val dimension: Int,
  override val df: DistanceFunction[T, D],
  override val distanceOrd: Ordering[D]
) extends Knn[T, D] {

  private case class Candidate(index: Int, distance: D)

  private val candidateOrd = new Ordering[Candidate] {
    override def compare(x: Candidate, y: Candidate): Int = distanceOrd.compare(x.distance, y.distance)
  }

  private val vectors   = new ArrayBuffer[Vector[T]]()
  private var nextIndex = 0

  override def add(vector: Vector[T]): Unit = {
    Utils.assert(vector.dimension == dimension, s"input vector dim=${vector.dimension}, knn dim=$dimension")
    vectors += vector
    nextIndex += 1
  }

  override def knn(vector: Vector[T], k: Int): Seq[Vector[T]] = {
    Utils.assert(vector.dimension == dimension, s"input vector dim=${vector.dimension}, knn dim=$dimension")
    val distances = new mutable.PriorityQueue[Candidate]()(candidateOrd.reverse)
    for (i <- vectors.indices) {
      distances.enqueue(Candidate(i, df(vectors(i), vector)))
    }
    distances.take(k).map(c => vectors(c.index)).toList
  }
}
