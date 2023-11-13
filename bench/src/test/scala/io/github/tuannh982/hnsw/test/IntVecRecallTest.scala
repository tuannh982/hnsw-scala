package io.github.tuannh982.hnsw.test

import io.github.tuannh982.hnsw.{BaselineHnsw, BruteForce, EuclidIntVectorDF, Vec}
import org.scalatest.flatspec.AnyFlatSpec

import scala.util.Random

/**
  * class only for playground, testing and tuning our models
  */
class IntVecRecallTest extends AnyFlatSpec {
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

  def calcRecall(answers: Seq[Vec[Int]], results: Seq[Vec[Int]]): Double = {
    val resultSet    = results.toSet
    val truePositive = answers.count(ans => resultSet.contains(ans))
    val recall       = truePositive.toDouble / answers.size
    recall
  }

  it should "calculate recall of baseline hnsw" ignore {
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
      48,
      ef = 100
    )
    model.allocate(nVectors)
    vectors.foreach { vector =>
      model.add(vector)
    }
    // calculate recall
    val nQueries = randRange(10000, 20000)
    val queries  = Array.fill(nQueries)(randomVector(-50, 50))
    println(s"number of query vectors: $nQueries")
    var recall = 0.0
    queries.foreach { query =>
      val k       = 10
      val results = baseline.knn(query, k)
      val answers = model.knn(query, k)
      val r       = calcRecall(answers, results)
      recall += r / nQueries
    }
    println(s"recall = $recall")
  }
}
