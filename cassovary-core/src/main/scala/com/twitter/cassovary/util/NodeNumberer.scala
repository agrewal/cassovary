/*
 * Copyright 2013 Twitter, Inc.
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
package com.twitter.cassovary.util

/*
 * Maintains a map from "node id" values read from source graph file, to some internal id value.
 * Implementer SequentialNodeNumberer uses this to map each input id to a sequentially increasing
 * value, effectively densifying the node id space and avoiding wasteful holes.
 * Implementer Identity does nothing to preserve existing behavior.
 * Usage: Graph reader calls nodeIdToNodeIdx(nodeId) on each read node.
 * On output, nodeIdxToNodeId(nodeIdx) called on each node index value to be output.
 */

trait NodeNumberer[@specialized(Int, Long) T] {
  def externalToInternal(externalNodeId: T): Int
  def internalToExternal(internalNodeId: Int): T
}

object NodeNumberer {
  final class IntIdentity extends NodeNumberer[Int] {
    def externalToInternal(externalNodeId: Int): Int = externalNodeId
    def internalToExternal(internalNodeId: Int): Int = internalNodeId
  }
}
