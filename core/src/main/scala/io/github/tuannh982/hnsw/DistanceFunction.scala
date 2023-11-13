package io.github.tuannh982.hnsw

import scala.language.higherKinds

trait DistanceFunction[T, D] {
  def apply(u: Vec[T], v: Vec[T]): D
}
