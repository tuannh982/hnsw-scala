package io.github.tuannh982.hnsw.test

import io.github.tuannh982.hnsw.{BaselineHnsw, BruteForce, EuclidIntVectorDF, Vec}
import org.scalatest.flatspec.AnyFlatSpec

import scala.util.Random

/**
 * class only for playground, testing and tuning our models
 */
class IntVecPrecisionTest extends AnyFlatSpec {
  private val random      = new Random()
  private val df          = new EuclidIntVectorDF()
  private val dimension   = 3
  private val distanceOrd = Ordering.Double
  private val vecOrd      = Vec.ordering[Int]()

  def randRange(l: Int, h: Int): Int = {
    l + random.nextInt(h - l)
  }

  def randomVector(l: Int, h: Int): Vec[Int] = {
    Vec(Array.fill(dimension)(randRange(l, h)))
  }

  // since the actual set size is equals to expected set size, so in our case, precision and recall are having exactly same value
  def calcPrecision(answers: Seq[Vec[Int]], results: Seq[Vec[Int]]): (Double, Double) = {
    val resultSet = results.toSet
    val masks = answers.map { ans =>
      if (resultSet.contains(ans)) {
        ans -> 1
      } else {
        ans -> 0
      }
    }
    val truePositive       = masks.filter(_._2 == 1)
    val falseNegative      = masks.filter(_._2 == 0)
    val truePositiveCount  = truePositive.size
    val falseNegativeCount = falseNegative.size
    val precision          = truePositiveCount.toDouble / answers.size
    val furthestResult     = results.max(vecOrd)
    val error = if (falseNegativeCount != 0) {
      falseNegative.map(fn => df(fn._1, furthestResult)).sum / falseNegativeCount
    } else {
      0.0
    }
    (precision, error)
  }

  it should "calculate precision of baseline hnsw" ignore {
    // generate data
    val nVectors = randRange(10000, 20000)
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
      m = 16,
      mL = 1.0 / math.log(16),
      40,
      ef = 100
    )
    model.allocate(nVectors)
    vectors.foreach { vector =>
      model.add(vector)
    }
    // calculate precision
    val nQueries = randRange(10000, 20000)
    val queries  = Array.fill(nQueries)(randomVector(-50, 50))
    println(s"number of query vectors: $nQueries")
    var precision = 0.0
    var error     = 0.0
    queries.foreach { query =>
      val k       = 10
      val results = baseline.knn(query, k)
      val answers = model.knn(query, k)
      val (p, e)  = calcPrecision(answers, results)
      precision += p / nQueries
      error += e / nQueries
    }
    println(s"precision = recall = $precision")
    println(s"avg error = $error")
  }
}
