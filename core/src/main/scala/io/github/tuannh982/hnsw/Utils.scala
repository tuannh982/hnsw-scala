package io.github.tuannh982.hnsw

import scala.reflect.ClassTag
import scala.util.Random

object Utils {
  val random   = new Random()
  val numCores = Runtime.getRuntime.availableProcessors()

  def assert(cond: => Boolean, errorMsg: => String): Unit = {
    if (!cond) {
      throw new AssertionError(errorMsg)
    }
  }

  def randVec[T: ClassTag](dimension: Int)(rng: => T): Vec[T] = {
    Vec(Array.fill(dimension)(rng))
  }
}
