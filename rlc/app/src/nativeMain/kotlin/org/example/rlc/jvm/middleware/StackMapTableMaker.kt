package org.example.rlc.jvm.middleware

import org.example.rlc.jvm.ir.ByteConstantOperation
import org.example.rlc.jvm.ir.ClassFile
import org.example.rlc.jvm.ir.CodeAttribute
import org.example.rlc.jvm.ir.ControlFlowOperation
import org.example.rlc.jvm.ir.MethodInfo
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

    codeAttribute.addAttribute(makeStackMapTable(codeAttribute.code, jumpTargets))
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

  // This function is out of its place.
  private fun makeStackMapTable(operations: List<Operation>, jumpTargets: List<Int>): StackMapTableAttribute {
    val stackTable = mutableListOf<StackMapFrame>()
    val offsets = calculateOffsets(operations)

//    val localVariables = composeArrayOfLocalVariables()

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

  private fun composeArrayOfLocalVariables(methodInfo: MethodInfo): List<VerificationTypeInfo> {
    val res = mutableListOf<VerificationTypeInfo>()

    res.addAll(methodInfo.signature.arguments)

    // For now this should do the trick for now.
    // Later, when I add local variables, I'll add more

    return res
  }
}
