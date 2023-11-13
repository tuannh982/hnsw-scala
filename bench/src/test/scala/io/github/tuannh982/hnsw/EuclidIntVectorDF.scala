package io.github.tuannh982.hnsw

import io.github.tuannh982.hnsw

class EuclidIntVectorDF extends DistanceFunction[Int, Double] {

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

class EuclidIntVectorDFWithCounter extends DistanceFunction[Int, Double] {

  private var counter = 0

  def resetCounter(): Unit = counter = 0
  def getCounter: Int      = counter

  override def apply(u: hnsw.Vec[Int], v: hnsw.Vec[Int]): Double = {
    Utils.assert(u.dimension == v.dimension, s"u dim=${u.dimension}, v dim=${v.dimension}")
    counter += 1
    var sumSquare = 0
    for (i <- u.arr.indices) {
      val diff = u.arr(i) - v.arr(i)
      sumSquare += diff * diff
    }
    math.sqrt(sumSquare)
  }
}
