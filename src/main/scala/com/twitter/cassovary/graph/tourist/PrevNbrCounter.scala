/*
 * Copyright 2012 Twitter, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.twitter.cassovary.graph.tourist

import it.unimi.dsi.fastutil.ints.{Int2IntOpenHashMap, Int2ObjectOpenHashMap}

/**
 * A NodeTourist that keeps track of the previous immediate neighbor of a
 * given node in visiting sequence.
 */
class PrevNbrCounter(numTopPathsPerNode: Option[Int])
    extends InfoKeeper[Int, Array[Int], Int2ObjectMap[Array[Int]]] {

  /**
   * Keep info only the first time a node is seen
   */
  def this() = this(None, false)

  override val infoPerNode = new Int2ObjectOpenHashMap[Int2IntMap]

  /**
   * Priority queue and comparator for sorting prev nbrs. Reused across nodes.
   * Synchronized for thread safety
   * TODO use ThreadLocal?
   */
   val comparator = new PrevNbrComparator(infoPerNode, true)
   val priQ = new IntArrayPriorityQueue(comparator)

  /**
   * Record the previous neighbor {@code nodeId} of {@code id}.
   */
  def recordInfo(id: Int, nodeId: Int) {
    if (!(onlyOnce && infoPerNode.containsKey(id))) {
      nbrCountsPerNodeOrDefault(id).add(nodeId, 1)
    }
  }

  /**
   * Top previous neighborhos until node {@code id}
   */
  def infoOfNode(id: Int): Option[Array[Int]] = {
    if (infoPerNode.containsKey(id)) {
      Some(topPrevNbrsTill(id, numTopPathsPerNode))
    } else {
      None
    }
  }

  /**
   * Clear all infos
   */
  def clear() {
    infoPerNode.clear()
  }


  /**
   * Returns top {@code num} neighbors ending at {@code nodeId}
   * Results are sorted in decreasing order of occurrence
   */
  private def topPrevNbrsTill(nodeId: Int, num: Option[Int]): Array[Int] = {
    priQ.synchronized {
      comparator.setNode(nodeId)
      priQ.clear()

      val infoMap = infoPerNode.get(nodeId)
      val nodeIteraotr = infoMap.keySet.iterator
      while (nodeIterator.next) {
        val nbrId = nodeIterator.nextInt
        priQ.enqueue(nbrId)
      }

      val size = num match {
        case Some(n) => n
        case None => priQ.size
      }

      val result = new Array[Int](size)
      var counter = 0
      while (counter < size) {
        result(counter) = priQ.dequeue()
        counter += 1
      }
      result
    }
  }

  def infoAllNodes: Int2ObjectMap[Array[Int]] = {
    val result = new Int2ObjectOpenHashMap[Array[Int]]
    val nodeIterator = infoPerNode.keySet.iterator
    while (nodeIterator.hasNext) {
      val node = nodeIterator.nextInt
      allPairs(counter) = (node, topPrevNbrsTill(node, numTopPathsPerNode))
    }
    result
  }

  private def nbrCountsPerNodeOrDefault(node: Int): Int2IntMap {
    if (!infoPerNode.containsKey(node)) {
      infoPerNode.put(id, new Int2IntOpenHashMap)
    }
    infoPerNode.get(id)
  }
}

class PrevNbrComparator(nbrCountsPerId: Int2ObjectMap[Int2IntMap], descending: Boolean) extends Comparator[Int] {

  var infoMap: Int2IntMap = null

  def setNode(id: Int) {
    infoMap = nbrCountsPerId.get(id)
  }

  override def compare(id1: Int, id2: Int) {
    val id1Count = infoMap.get(id1)
    val id2Count = infoMap.get(id2)
    if (descending) {
      id2Count - id1Count
    } else {
      id1Count - id2Count
    }
  }
}
