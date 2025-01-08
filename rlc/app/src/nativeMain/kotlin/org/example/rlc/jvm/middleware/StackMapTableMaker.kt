package org.example.rlc.jvm.middleware

import org.example.rlc.jvm.ir.ByteConstantOperation
import org.example.rlc.jvm.ir.ClassFile
import org.example.rlc.jvm.ir.CodeAttribute
import org.example.rlc.jvm.ir.ControlFlowOperation
import org.example.rlc.jvm.ir.MethodInfo
import org.example.rlc.jvm.ir.Opcode
import org.example.rlc.jvm.ir.Operation
import org.example.rlc.jvm.ir.ShortConstantOperation
import org.example.rlc.jvm.ir.SimpleOperation
import org.example.rlc.jvm.ir.StackMapFrame
import org.example.rlc.jvm.ir.StackMapTableAttribute
import org.example.rlc.jvm.ir.VerificationTypeInfo

class StackMapTableMaker {
  fun fillStackMapTables(classes: List<ClassFile>) {
    classes.forEach { classFile ->
      processClass(classFile)
    }
  }

  private fun processClass(classFile: ClassFile) {
    classFile.methodList.forEach(this::addAttributeIfRequired)
  }

  private fun addAttributeIfRequired(methodInfo: MethodInfo) {
    if (methodInfo.isAbstract) {
      return
    }

    val codeAttribute = extractCodeFromMethodInfo(methodInfo)
    val jumpTargets = calculateJumpTargets(codeAttribute.code)
    val localVariables = composeArrayOfLocalVariables(methodInfo)

    codeAttribute.addAttribute(makeStackMapTable(
      codeAttribute.code,
      localVariables,
      jumpTargets
    ))
  }

  private fun extractCodeFromMethodInfo(methodInfo: MethodInfo): CodeAttribute {
    var codeAttribute: CodeAttribute? = null
    for (attr in methodInfo.attributeList) {
      when (attr) {
        is CodeAttribute -> codeAttribute = attr
        else -> {}
      }
    }

    if (codeAttribute == null) {
      throw IllegalStateException(
        "Method Info [${methodInfo.methodName.encodedString}] " +
            "has no code attributes. IsAbstract: ${methodInfo.isAbstract}"
      )
    }

    return codeAttribute
  }

  private fun calculateJumpTargets(operationList: List<Operation>): List<Int> {
    val res = mutableListOf<Int>()
    for (operation in operationList) {
      when (operation) {
        is ControlFlowOperation -> res.add(operation.jumpTo)
        else -> continue
      }
    }

    return res
  }

  // This function feels out of its place.
  private fun makeStackMapTable(
    operations: List<Operation>,
    localVariables: List<VerificationTypeInfo>,
    jumpTargets: List<Int>
  ): StackMapTableAttribute {
    if (jumpTargets.isEmpty()) {
      return StackMapTableAttribute(frames = listOf())
    }

    val stackTable = mutableListOf<StackMapFrame>()
    val offsets = calculateOffsets(operations)

    return StackMapTableAttribute(frames = stackTable)
  }

  private fun calculateOffsets(operations: List<Operation>): List<Int> {
    val res = mutableListOf<Int>()
    var offset = 0

    for (op in operations) {
      res.add(offset)

      offset += when (op) {
        is ControlFlowOperation -> 2
        is ByteConstantOperation -> 2
        is ShortConstantOperation -> 3
        is SimpleOperation -> 1
      }
    }

    return res
  }

  private fun makeControlFlowGraph(ops: List<Operation>): CfgNode {
    val nodeIndex = mutableMapOf<Int, CfgNode>()

    var currentNodeOps = mutableListOf<Operation>()
    var startingIndex = 0
    for ((ind, op) in ops.withIndex()) {
      currentNodeOps.add(op)

      if (lastInNodeOperation(op)) {
        val node = CfgNode(startingIndex = startingIndex, ops = currentNodeOps)
        currentNodeOps = mutableListOf()
        nodeIndex[startingIndex] = node
        startingIndex = ind + 1
      }
    }

    for ((ind, node) in nodeIndex) {
      // TODO: add children
    }

    return nodeIndex[0] ?: throw IllegalStateException("Control flow graph should have at least one node.")
  }

  private fun lastInNodeOperation(op: Operation) = when(op.opcode) {
    Opcode.OP_RETURN -> true
    Opcode.OP_IRETURN -> true
    Opcode.OP_IFEQ -> true
    Opcode.OP_IFNE -> true
    Opcode.OP_ARETURN -> true
    else -> false
  }

  private fun composeArrayOfLocalVariables(methodInfo: MethodInfo): List<VerificationTypeInfo> {
    val res = mutableListOf<VerificationTypeInfo>()

    res.addAll(methodInfo.signature.arguments)

    // For now this should do the trick for now.
    // Later, when I add local variables, I'll add more

    return res
  }
}
