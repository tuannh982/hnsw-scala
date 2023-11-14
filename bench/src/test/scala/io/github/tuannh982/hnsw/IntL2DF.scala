package io.github.tuannh982.hnsw

import io.github.tuannh982.hnsw

/**
  * L2 distance function
  */
class IntL2DF extends DistanceFunction[Int, Double] {

  override def apply(u: hnsw.Vec[Int], v: hnsw.Vec[Int]): Double = {
    Utils.assert(u.dimension == v.dimension, s"u dim=${u.dimension}, v dim=${v.dimension}")
    var sumSquare = 0
    for (i <- u.arr.indices) {
      val diff = u.arr(i) - v.arr(i)
      sumSquare += diff * diff
    }
    math.sqrt(sumSquare)
  }
}
