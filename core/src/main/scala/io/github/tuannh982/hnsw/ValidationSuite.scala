package io.github.tuannh982.hnsw

import java.util.concurrent.Executors
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

class ValidationSuite[T, D](
  val dimension: Int,
  val df: DistanceFunction[T, D],
  val distanceOrd: Ordering[D]
) {

  /**
    * calculate model recall
    * @param answers set of answers vectors
    * @param results the actual answers (or results)
    * @return recall
    */
  private def calculateRecall(answers: Seq[Vec[T]], results: Seq[Vec[T]]): Double = {
    val resultSet    = results.toSet
    val truePositive = answers.count(ans => resultSet.contains(ans))
    val recall       = truePositive.toDouble / answers.size
    recall
  }

  /**
    * perform model validation
    * @param dataset the input dataset, used to train the model
    * @param validationDataset the validation dataset
    * @param k number of neighbors to query
    * @param model the input model to validate
    * @param numThreads number of thread to run the validation
    * @return recall
    */
  def validate(
    dataset: Seq[Vec[T]],
    validationDataset: Seq[Vec[T]],
    k: Int,
    model: BaseGraph[T, D],
    numThreads: Int
  ): Double = {
    // assertions
    Utils.assert(
      model.dimension == dimension,
      s"model dimension is mismatched, expected: $dimension, actual ${model.dimension}"
    )
    // building models
    val validationModel = new BruteForce(dimension, df, distanceOrd)
    dataset.foreach { v =>
      validationModel.add(v)
      model.add(v)
    }
    // split validation dataset
    val validationDatasetSize = validationDataset.size
    val subValidationSetSize  = (validationDatasetSize.toDouble / numThreads).round.toInt
    val split                 = validationDataset.grouped(subValidationSetSize).toArray
    Utils.assert(split.length == numThreads, s"unexpected split size, expected = $numThreads, actual = ${split.length}")
    val pool      = Executors.newFixedThreadPool(numThreads)
    val ec        = ExecutionContext.fromExecutor(pool)
    val recallFut = new ArrayBuffer[Future[Double]]()
    for (i <- 0 until numThreads) {
      recallFut += Future {
        var recall  = 0.0
        val queries = split(i)
        queries.foreach { query =>
          val results = validationModel.knn(query, k)
          val answers = model.knn(query, k)
          recall += calculateRecall(answers, results) / validationDatasetSize
        }
        recall
      }(ec)
    }
    implicit val joinEc: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
    val f                                 = Future.sequence(recallFut).map(_.sum)
    val recall                            = Await.result(f, Duration.Inf)
    pool.shutdown()
    recall
  }
}
