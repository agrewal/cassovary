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

import org.specs.Specification

class DirectedPathCollectionSpec extends Specification {

  "path collection out of the same node" should {

    val testPathIds = Array(10, 11, 12, 14, 15, 11, 14, 0)

    def getPath(ids: Int*) = DirectedPath(ids.toArray.map { testPathIds(_) })

    def addPath(coll: DirectedPathCollection, idsInPath: List[Int]) {
      idsInPath foreach { id =>
        coll.appendToCurrentPath(testPathIds(id))
      }
    }

    "1 path sequence with 3 nodes" in {
      val paths = new DirectedPathCollection

      (1 to 2) foreach { times =>
        paths.resetCurrentPath()
        addPath(paths, List(0,1,2))

        val left = paths.topPathsTill(testPathIds(0), 10)
        val right = Array((getPath(0), times))
        println((left(0)._1, left(0)._2, right(0)._1, right(0)._2))
        paths.topPathsTill(testPathIds(0), 10) mustEqual Array((getPath(0), times))
        paths.topPathsTill(testPathIds(1), 10) mustEqual Array((getPath(0, 1), times))
        paths.topPathsTill(testPathIds(2), 10) mustEqual Array((getPath(0, 1, 2), times))

        List(0,1,2) foreach { id =>
          paths.numUniquePathsTill(testPathIds(id)) mustEqual 1
        }

        paths.totalNumPaths mustEqual 3
      }
    }

    "3 paths with 4 nodes" in {
      val paths = new DirectedPathCollection
      addPath(paths, List(0,1,2))
      paths.resetCurrentPath()
      addPath(paths, List(1,2,3))
      paths.resetCurrentPath()
      addPath(paths, List(1,0,3,2,3))
      paths.resetCurrentPath()
      addPath(paths, List(1,2,3))

      //println(paths.topPathsTill(testPathIds(0), 10))
      //println(List((getPath(0), 1), (getPath(1, 0), 1)))
      paths.topPathsTill(testPathIds(0), 10) mustEqual List((getPath(0), 1), (getPath(1, 0), 1))
      paths.topPathsTill(testPathIds(1), 10) mustEqual List((getPath(1), 3), (getPath(0, 1), 1))
      paths.topPathsTill(testPathIds(2), 10) mustEqual List(
        (getPath(1, 2), 2),
        (getPath(0, 1, 2), 1),
        (getPath(1, 0, 3, 2), 1)
        )
      paths.topPathsTill(testPathIds(3), 10) mustEqual List(
        (getPath(1, 2, 3), 2),
        (getPath(1, 0, 3), 1),
        (getPath(1, 0, 3, 2, 3), 1)
        )

      paths.topPathsTill(testPathIds(2), 2) mustEqual List(
        (getPath(1, 2), 2),
        (getPath(0, 1, 2), 1)
        )

      paths.numUniquePathsTill(testPathIds(0)) mustEqual 2
      paths.numUniquePathsTill(testPathIds(1)) mustEqual 2
      paths.numUniquePathsTill(testPathIds(2)) mustEqual 3
      paths.numUniquePathsTill(testPathIds(3)) mustEqual 3

      paths.totalNumPaths mustEqual 10
    }
  }
}
