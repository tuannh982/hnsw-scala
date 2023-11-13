package io.github.tuannh982.hnsw

import scala.language.higherKinds

trait DistanceFunction[T, D] {
  def apply(u: Vector[T], v: Vector[T]): D
}
