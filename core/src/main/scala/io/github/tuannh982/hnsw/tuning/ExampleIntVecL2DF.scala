package io.github.tuannh982.hnsw.tuning

import io.github.tuannh982.hnsw.{DistanceFunction, RefHnsw, Utils, ValidationSuite, Vec}

object ExampleIntVecL2DF {

  /**
    * L2 distance function
    */
  class IntL2DF extends DistanceFunction[Int, Double] {

    override def apply(u: Vec[Int], v: Vec[Int]): Double = {
      Utils.assert(u.dimension == v.dimension, s"u dim=${u.dimension}, v dim=${v.dimension}")
      var sumSquare = 0
      for (i <- u.arr.indices) {
        val diff = u.arr(i) - v.arr(i)
        sumSquare += diff * diff
      }
      math.sqrt(sumSquare)
    }
  }

  private def rand(l: Int, h: Int): Int = l + Utils.random.nextInt(h - l + 1)

  def main(args: Array[String]): Unit = {
    val dimension   = 20
    val df          = new IntL2DF()
    val distanceOrd = Ordering.Double
    val suite = new ValidationSuite[Int, Double](
      dimension = dimension,
      df = df,
      distanceOrd = distanceOrd
    )
    // generate input
    val inputSize         = 1000
    val dataset           = Array.fill(inputSize)(Utils.randVec(dimension)(rand(-10, 10)))
    val validationSize    = 80000
    val validationDataset = Array.fill(validationSize)(Utils.randVec(dimension)(rand(-10, 10)))
    val k                 = 10
    // init model
    val model = new RefHnsw(dimension, df, distanceOrd)
    model.allocate(inputSize)
    // validate our model
    val recall = suite.validate(
      dataset = dataset,
      validationDataset = validationDataset,
      k = k,
      model = model,
      numThreads = Utils.numCores * 2
    )
    println(s"recall = $recall")
  }
}
