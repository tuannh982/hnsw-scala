package io.github.tuannh982.hnsw

import io.github.tuannh982.hnsw.RefHnsw.{CandidateList, EntryPoint, NeighborList}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.util.Random
import scala.util.control.Breaks.{break, breakable}

/**
  * Reference implementation of HNSW algorithm described in paper https://arxiv.org/pdf/1603.09320.pdf
  *
  * **note**: this is a reference implementation, it only focus on correctness, not about performance
  */
class RefHnsw[T, D](
  override val dimension: Int,
  override val df: DistanceFunction[T, D],
  override val distanceOrd: Ordering[D],
  val m: Int = 16,
  val mL: Double = 1.0 / math.log(16),
  val efConstruction: Int = 20,
  val ef: Int = 100
) extends BaseGraph[T, D] {

  private val maxM                                      = m + 1
  private val maxM0                                     = m * 2 + 1
  private val candidateOrd                              = Candidate.fromDistanceOrd(distanceOrd)
  private var vectors: Array[Vec[T]]                    = _
  private var nextIndex                                 = 0
  private var neighborGraph: Array[Array[NeighborList]] = _
  private var hnswEntryPoint: Option[EntryPoint]        = None
  private val random                                    = new Random()

  /**
    * pre-allocate space for the model. must only called once before inserting any vectors
    * @param n total number of vectors
    */
  def allocate(n: Int): Unit = {
    vectors = new Array[Vec[T]](n)
    neighborGraph = new Array[Array[NeighborList]](n)
  }

  private def maxNeighborCount(level: Int) = if (level == 0) maxM0 else maxM

  private def getOrElseUpdateEntryPoint(index: Int, level: Int): EntryPoint = {
    hnswEntryPoint match {
      case Some(value) =>
        value
      case None =>
        val newEntry = EntryPoint(index, level)
        hnswEntryPoint = Some(newEntry)
        newEntry
    }
  }

  private def addVector(vector: Vec[T]): Int = {
    val index = nextIndex
    vectors(index) = vector
    nextIndex += 1
    index
  }

  /**
    * reserve space in the graph when a new node is added
    * @param index node index
    * @param level node level
    */
  private def allocateGraphSpace(index: Int, level: Int): Unit = {
    neighborGraph(index) = new Array[NeighborList](level + 1)
    for (l <- 0 to level) {
      neighborGraph(index)(l) = NeighborList(new ArrayBuffer[Int]())
    }
  }

  // Algorithm 2
  // SEARCH-LAYER(q, ep, ef, lc)
  // Input: query element q, enter points ep, number of nearest to q elements to return ef, layer number lc
  // Output: ef closest neighbors to q
  private def searchLayer(q: Vec[T], ep: Int, ef: Int, lc: Int): Seq[Candidate[D]] = {
    // v ← ep // set of visited elements
    // W ← ep // dynamic list of found nearest neighbors
    // C ← ep // set of candidates
    val v = new mutable.HashSet[Int]()
    val W = CandidateList(candidateOrd)
    val C = CandidateList(candidateOrd.reverse)
    // add candidate to W and C
    def addCandidate(index: Int): Unit = {
      val candidate = Candidate(index, df(vectors(index), q))
      W.push(candidate)
      C.push(candidate)
    }
    // add ep to those lists
    v.add(ep)
    addCandidate(ep)
    // while │C│ > 0
    breakable {
      while (C.nonEmpty) {
        // c ← extract nearest element from C to q
        // f ← get furthest element from W to q
        val c = C.pop()
        var f = W.peek()
        // if distance(c, q) > distance(f, q)
        if (distanceOrd.gt(c.distance, f.distance)) {
          // break // all elements in W are evaluated
          break
        } else {
          // for each e ∈ neighbourhood(c) at layer lc // update C and W
          val neighbourhood = neighborGraph(c.index)(lc)
          neighbourhood.arr.foreach { e =>
            // if e ∉ v
            if (!v.contains(e)) {
              // v ← v ⋃ e
              v.add(e)
              // f ← get furthest element from W to q
              f = W.peek()
              // if distance(e, q) < distance(f, q) or │W│ < ef
              if (distanceOrd.lt(df(vectors(e), q), df(vectors(f.index), q)) || W.size < ef) {
                // C ← C ⋃ e
                // W ← W ⋃ e
                addCandidate(e)
                // if │W│ > ef
                if (W.size > ef) {
                  // remove furthest element from W to q
                  W.pop()
                }
              }
            }
          }
        }
      }
    }
    W.toList
  }

  // Algorithm 3
  // SELECT-NEIGHBORS-SIMPLE(q, C, M)
  // Input: base element q, candidate elements C, number of neighbors to
  // return M
  // Output: M nearest elements to q
  // return M nearest elements from C to q

  // SELECT-NEIGHBORS(q, W, M, lc)
  private def selectNeighbors1Alg3(candidates: CandidateList[D], M: Int): Seq[Candidate[D]] = {
    candidates.toList.sorted(candidateOrd).take(M)
  }

  // SELECT-NEIGHBORS(e, eConn, Mmax, lc)
  private def selectNeighbors2Alg3(e: Int, C: Seq[Int], M: Int): Seq[Candidate[D]] = {
    val vector        = vectors(e)
    val candidateList = CandidateList(candidateOrd.reverse)
    C.foreach { candidate =>
      candidateList.push(Candidate(candidate, df(vectors(candidate), vector)))
    }
    candidateList.take(M)
  }

  // Algorithm 1
  // INSERT(hnsw, q, M, Mmax, efConstruction, mL)
  // Input: multilayer graph hnsw, new element q, number of established
  // connections M, maximum number of connections for each element
  // per layer Mmax, size of the dynamic candidate list efConstruction, normalization factor for level generation mL
  // Output: update hnsw inserting element q
  override def add(vector: Vec[T]): Unit = {
    Utils.assert(vector.dimension == dimension, s"input vector dim=${vector.dimension}, knn dim=$dimension")
    val q     = vector
    val index = addVector(vector)
    // l ← ⌊-ln(unif(0..1))∙mL⌋ // new element’s level
    val level = (-math.log(random.nextDouble()) * mL).floor.toInt
    allocateGraphSpace(index, level)
    // W ← ∅ // list for the currently found nearest elements
    val W = CandidateList(candidateOrd.reverse)
    // ep ← get enter point for hnsw
    // L ← level of ep // top layer for hnsw
    val currentEntryPoint = getOrElseUpdateEntryPoint(index, level)
    var ep                = currentEntryPoint.index
    val topLevel          = currentEntryPoint.level
    // for lc ← L … l+1
    for (lc <- topLevel to level + 1 by -1) {
      // W ← SEARCH-LAYER(q, ep, ef=1, lc)
      W.pushAll(searchLayer(q, ep, 1, lc))
      // ep ← get the nearest element from W to q
      ep = W.pop().index
    }
    // for lc ← min(L, l) … 0
    for (lc <- math.min(topLevel, level) to 0 by -1) {
      val maxNeighborCountAtLc = maxNeighborCount(lc)
      // W ← SEARCH-LAYER(q, ep, efConstruction, lc)
      W.pushAll(searchLayer(q, ep, efConstruction, lc))
      // neighbors ← SELECT-NEIGHBORS(q, W, M, lc) // alg. 3 or alg. 4
      val neighbors = selectNeighbors1Alg3(W, maxNeighborCountAtLc)
      // add bidirectional connections from neighbors to q at layer lc
      neighbors.foreach { neighbor =>
        if (neighbor.index != index) { // don't self link
          neighborGraph(neighbor.index)(lc).arr += index
          neighborGraph(index)(lc).arr += neighbor.index
        }
      }
      // for each e ∈ neighbors // shrink connections if needed
      neighbors.foreach { neighbor =>
        // eConn ← neighbourhood(e) at layer lc
        val eConn = neighborGraph(neighbor.index)(lc)
        // if │eConn│ > Mmax // shrink connections of e
        //                   // if lc = 0 then Mmax = Mmax0
        if (eConn.arr.size > maxNeighborCountAtLc) {
          // eNewConn ← SELECT-NEIGHBORS(e, eConn, Mmax, lc)
          //                             // alg. 3 or alg. 4
          val eNewConn = selectNeighbors2Alg3(neighbor.index, eConn.arr, maxNeighborCountAtLc)
          // set neighbourhood(e) at layer lc to eNewConn
          neighborGraph(neighbor.index)(lc) = NeighborList(ArrayBuffer(eNewConn.map(_.index): _*))
        }
      }
      // ep ← W
      ep = W.pop().index
    }
    // if l > L
    // set enter point for hnsw to q
    if (level > topLevel) {
      hnswEntryPoint = Some(EntryPoint(index, level))
    }
  }

  // Algorithm 5
  // K-NN-SEARCH(hnsw, q, K, ef)
  // Input: multilayer graph hnsw, query element q, number of nearest
  // neighbors to return K, size of the dynamic candidate list ef
  // Output: K nearest elements to q
  override def knn(vector: Vec[T], k: Int): Seq[Vec[T]] = {
    Utils.assert(vector.dimension == dimension, s"input vector dim=${vector.dimension}, knn dim=$dimension")
    val q = vector
    // W ← ∅ // set for the current nearest elements
    val W = CandidateList(candidateOrd.reverse)
    // ep ← get enter point for hnsw
    // L ← level of ep // top layer for hnsw
    val currentEntryPoint = hnswEntryPoint.get // the entry point must be set, or else we will throw since there's no vectors in the model
    var ep                = currentEntryPoint.index
    val topLevel          = currentEntryPoint.level
    // for lc ← L … 1
    for (lc <- topLevel to 1 by -1) {
      // W ← SEARCH-LAYER(q, ep, ef=1, lc)
      W.pushAll(searchLayer(q, ep, 1, lc))
      // ep ← get nearest element from W to q
      ep = W.pop().index
    }
    // W ← SEARCH-LAYER(q, ep, ef, lc =0)
    W.pushAll(searchLayer(q, ep, ef, 0))
    W.take(k).map(c => vectors(c.index))
  }
}

object RefHnsw {
  case class EntryPoint(index: Int, level: Int)

  case class NeighborList(arr: ArrayBuffer[Int])

  case class CandidateList[D](ord: Ordering[Candidate[D]]) {
    private val lst = new mutable.HashSet[Int]()
    private val pq  = new mutable.PriorityQueue[Candidate[D]]()(ord)

    def push(candidates: Candidate[D]*): Unit = pushAll(candidates)

    def pushAll(candidates: Seq[Candidate[D]]): Unit = {
      candidates.foreach { candidate =>
        if (!lst.contains(candidate.index)) {
          lst.add(candidate.index)
          pq.enqueue(candidate)
        }
      }
    }

    def peek(): Candidate[D] = pq.head

    def pop(): Candidate[D] = {
      val head = pq.head
      lst.remove(head.index)
      pq.dequeue()
    }

    def nonEmpty: Boolean         = pq.nonEmpty
    def isEmpty: Boolean          = pq.isEmpty
    def toList: Seq[Candidate[D]] = pq.toList
    def size: Int                 = pq.size

    def take(n: Int): Seq[Candidate[D]] = {
      toList.sorted(ord.reverse).take(n)
    }
  }
}
