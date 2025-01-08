package org.example.rlc.jvm.middleware

import org.example.rlc.jvm.ir.Operation

internal class CfgNode(
  internal val ops: List<Operation>,
  internal val startingIndex: Int,
) {
  private val _children: MutableList<CfgNode> = mutableListOf()

  internal fun addChild(node: CfgNode) {
    _children.add(node)
  }

  internal val children: List<CfgNode> = _children.toList()

  internal val lastOp = _children.last()

  internal val lastInd = startingIndex + _children.size
}
