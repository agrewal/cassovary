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
package com.twitter.cassovary.graph

import it.unimi.dsi.fastutil.Arrays
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import it.unimi.dsi.fastutil.ints.{Int2ObjectOpenHashMap, IntComparator}


/**
 * Represents a collection of directed paths. For example, a collection of paths out of
 * the same source node. This class is *not* thread safe!
 */
class DirectedPathCollection {

  // the current path being built
  private val currPath = DirectedPath.builder()

  // pathCountsPerId(id)(path) = count of #times path has been seen ending in id
  private val pathCountsPerId = new Int2ObjectOpenHashMap[Object2IntOpenHashMap[DirectedPath]]

  // int comparator and swapper for sorting directed path
  private val directedPathIntComparator = new DirectedPathIntComparator
  private val directedPathArraySwapper = new DirectedPathArraySwapper

  /**
   * Appends node to the current path and record this path against the node.
   * @param node the node being visited
   */
  def appendToCurrentPath(node: Int) {
    currPath.append(node)
    pathCountsPerIdWithDefault(node).add(currPath.snapshot, 1)
  }

  /**
   * Reset the current path
   */
  def resetCurrentPath() {
    currPath.clear()
  }

  /**
   * @return a list of tuple(path, count) where count is the number of times path
   *         ending at {@code node} is observed
   */
  def topPathsTill(node: Int, num: Int) = {
    val pathCounterMap = pathCountsPerIdWithDefault(node)
    val pathCount = pathCounterMap.size
    val pathArray = new Array[DirectedPath](pathCount)

    directedPathIntComparator.pathCounterMap = pathCounterMap
    directedPathIntComparator.pathArray = pathArray
    directedPathArraySwapper.pathArray = pathArray

    val pathArray = new Array[DirectedPath](pathCount)
    pathCounterMap.keySet.toArray(pathArray)
    Arrays.quicksort(0, pathCount, directedPathIntComparator, directedPathArraySwapper)

    // TODO make this an array of ... tuples?
    pathArray.toSeq.take(num).map { path => (path, pathCounterMap.getInt(path)) }
  }

  /**
   * @param num the number of top paths to return for a node
   * @return a mapping of node to the list of top paths ending at node
   */
  def topPathsPerNodeId(num: Int): collection.Map[Int, List[(DirectedPath, Int)]] = {
    Map.empty ++ pathCountsPerId.keysIterator.map { node =>
        (node, topPathsTill(node, num))
    }
  }

  /**
   * @return the total number of unique paths that end at {@code node}
   */
  def numUniquePathsTill(node: Int) = pathCountsPerIdWithDefault(node).size

  /**
   * @return the total number of unique paths in this collection
   */
  def totalNumPaths = {
    pathCountsPerId.keysIterator.foldLeft(0) { case (tot, node) => tot + numUniquePathsTill(node) }
  }

  private def pathCountsPerIdWithDefault(node: Int) = {
    if (!pathCountsPerId.containsKey(node)) {
      val map = new Object2IntOpenHashMap[DirectedPath]
      pathCountsPerId.put(node, map)
    }
    pathCountsPerId.get(node)
  }

}

class DirectedPathIntComparator extends IntComparator {
  var pathCounterMap
  var pathArray

  override def compare(a: Int, b: Int) = {
    val aCount = pathCounterMap.getInt(pathArray(a))
    val bCount = pathCounterMap.getInt(pathArray(b))
    bCount - aCount
  }
}

class DirectedPathArraySwapper extends Swapper {
  var pathArray

  override def swap(a: Int, b: Int) {
    val temp = pathArray(a)
    pathArray(a) = pathArray(b)
    pathArray(b) = temp
  }
}