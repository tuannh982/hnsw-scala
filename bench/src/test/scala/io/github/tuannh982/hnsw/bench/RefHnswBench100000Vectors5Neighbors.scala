package io.github.tuannh982.hnsw.bench

import io.github.tuannh982.hnsw.{BaseGraph, BruteForce, IntL2DF, RefHnsw, Vec}
import org.openjdk.jmh.annotations.{Benchmark, BenchmarkMode, Mode, Scope, State}

import scala.util.Random

@State(Scope.Benchmark)
@BenchmarkMode(Array(Mode.Throughput))
class RefHnswBench100000Vectors5Neighbors {
  private val nVectors                      = 100000
  private val k                             = 5
  private val random                        = new Random()
  private val df                            = new IntL2DF()
  private val dimension                     = 64
  private val distanceOrd                   = Ordering.Double
  private var model: BaseGraph[Int, Double] = _

  private def randomVector(l: Int, h: Int): Vec[Int] = {
    Vec(Array.fill(dimension)(l + random.nextInt(h - l)))
  }

  private def init(): Unit = {
    val vectors = Array.fill(nVectors)(randomVector(-50, 50))
    val hnsw = new RefHnsw(
      dimension,
      df,
      distanceOrd
    )
    hnsw.allocate(nVectors)
    vectors.foreach { vector =>
      hnsw.add(vector)
    }
    model = hnsw
  }

  init()

  @Benchmark
  def query(): Seq[Vec[Int]] = {
    val query = randomVector(-50, 50)
    model.knn(query, k)
  }
}
