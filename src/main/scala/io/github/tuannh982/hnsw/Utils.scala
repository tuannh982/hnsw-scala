package io.github.tuannh982.hnsw

object Utils {
  def assert(cond: => Boolean, errorMsg: => String): Unit = {
    if (!cond) {
      throw new AssertionError(errorMsg)
    }
  }
}
