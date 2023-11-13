package io.github.tuannh982.hnsw.test

import io.github.tuannh982.hnsw.{BaselineHnsw, BruteForce, EuclidIntVectorDF, Vec}
import org.scalatest.flatspec.AnyFlatSpec

import scala.util.Random

class IntVecPrecisionTest extends AnyFlatSpec {
  private val random      = new Random()
  private val df          = new EuclidIntVectorDF()
  private val dimension   = 3
  private val distanceOrd = Ordering.Double

  def randomVector(l: Int, h: Int): Vec[Int] = {
    Vec(Array.fill(dimension)(l + random.nextInt(h)))
  }

  // since the actual set size is equals to expected set size, so in our case, precision and recall are having exactly same value
  def calcPrecision(answers: Seq[Vec[Int]], results: Seq[Vec[Int]]): Double = {
    val resultSet = results.toSet
    val truePositive = answers.map { ans =>
      if (resultSet.contains(ans)) {
        1
      } else {
        0
      }
    }.sum
    truePositive.toDouble / answers.size
  }

  it should "calculate precision of baseline hnsw" ignore {
    // generate data
    val nVectors = 10000 + random.nextInt(10000)
    val vectors  = Array.fill(nVectors)(randomVector(-50, 50))
    println(s"number of generated vectors: $nVectors")
    // load baseline model
    val baseline = new BruteForce(dimension, df, distanceOrd)
    vectors.foreach { vector =>
      baseline.add(vector)
    }
    // load our model
    val model = new BaselineHnsw(
      dimension,
      df,
      distanceOrd,
      m = 32,
      mL = 1.0 / math.log(32),
      64,
      ef = 100
    )
    model.allocate(nVectors)
    vectors.foreach { vector =>
      model.add(vector)
    }
    // calculate precision
    val nQueries = 10000 + random.nextInt(10000)
    val queries  = Array.fill(nQueries)(randomVector(-50, 50))
    println(s"number of query vectors: $nQueries")
    var precision = 0.0
    queries.foreach { query =>
      val k       = 5 + random.nextInt(5)
      val results = baseline.knn(query, k)
      val answers = model.knn(query, k)
      precision += calcPrecision(answers, results) / nQueries
    }
    println(s"precision/recall $precision")
  }
}
