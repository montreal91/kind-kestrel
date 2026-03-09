package org.example.rlc.jvm.middleware

import org.example.rlc.jvm.ir.ByteConstantOperation
import org.example.rlc.jvm.ir.ClassFile
import org.example.rlc.jvm.ir.ClassInfo
import org.example.rlc.jvm.ir.CodeAttribute
import org.example.rlc.jvm.ir.ControlFlowOperation
import org.example.rlc.jvm.ir.DoubleVti
import org.example.rlc.jvm.ir.EmptyVti
import org.example.rlc.jvm.ir.FullFrame
import org.example.rlc.jvm.ir.FullFrameBuilder
import org.example.rlc.jvm.ir.IntegerVti
import org.example.rlc.jvm.ir.MethodInfo
import org.example.rlc.jvm.ir.MethodRefInfo
import org.example.rlc.jvm.ir.NullVariableVti
import org.example.rlc.jvm.ir.ObjectVti
import org.example.rlc.jvm.ir.Opcode
import org.example.rlc.jvm.ir.Operation
import org.example.rlc.jvm.ir.OperationWithIndex
import org.example.rlc.jvm.ir.ShortConstantOperation
import org.example.rlc.jvm.ir.SimpleOperation
import org.example.rlc.jvm.ir.StackMapTableAttribute
import org.example.rlc.jvm.ir.VerificationTypeInfo

/**
 * This class is responsible for generating and populating Stack Map Tables for a set of provided class files.
 * It processes each class file and adds the required attributes to methods based on their code and control flow.
 */
class StackMapTableMaker {
  fun fillStackMapTables(classes: List<ClassFile>) {
    classes.forEach { classFile ->
      processClass(classFile)
    }
  }

  private fun processClass(classFile: ClassFile) {
    println("\n")
    println("=============================================================")
    println("Adding attributes if required for class ${classFile.filename}")
    for (method in classFile.methodList) {
      addAttributeIfRequired(method, classFile.thisClassInfo)
    }
  }

  private fun addAttributeIfRequired(methodInfo: MethodInfo, thisClassInfo: ClassInfo) {
    if (methodInfo.isAbstract) {
      return
    }

    val codeAttribute = extractCodeFromMethodInfo(methodInfo)
    val jumpTargets = calculateJumpTargets(codeAttribute.code)
    val localVariables = composeArrayOfLocalVariables(methodInfo, thisClassInfo)

    println("\nMaking Stack Map Table attribute for ${methodInfo.methodName.encodedString}")
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
        is ControlFlowOperation -> res.add(operation.getJumpTo())
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

    val offsets = calculateOffsets(operations)
    println("Offsets")
    for ((i, offset) in offsets.withIndex()) {
      val suffix = when (val op = operations[i]) {
        is OperationWithIndex -> "(index=${op.index})"
        is ShortConstantOperation -> when (op.opcode) {
          Opcode.OP_GETFIELD, Opcode.OP_PUTFIELD -> "(field=${op.constant})"
          Opcode.OP_INSTANCEOF -> "(class=${op.constant})"
          else -> ""
        }
        else -> ""
      }
      println("$i, $offset, ${operations[i].opcode}$suffix")
    }

    val frames = List(operations.size) { FullFrameBuilder() }
    var maxStackSize = 0
    val stack = ArrayDeque<VerificationTypeInfo>()

    val currentVariables = localVariables.toMutableList()

    localVariables.forEach {
      println("    $it")
    }

    for ((i, _) in operations.withIndex()) {
      println("$i, ${operations[i].opcode}")
      when (operations[i].opcode) {
        Opcode.OP_ACONST_NULL -> stack.addLast(NullVariableVti())
        Opcode.OP_ALOAD_0 -> stack.addLast(currentVariables[0])
        Opcode.OP_ALOAD_1 -> stack.addLast(currentVariables[1])
        Opcode.OP_ALOAD_2 -> stack.addLast(currentVariables[2])
        Opcode.OP_ALOAD_3 -> stack.addLast(currentVariables[3])
        Opcode.OP_ALOAD -> {
          val op = operations[i] as OperationWithIndex
          stack.addLast(currentVariables[op.index.toInt()])
        }

        Opcode.OP_ARETURN -> stack.clear()
        Opcode.OP_ASTORE_2 -> {
          stack.removeLast()
          val op = operations[i] as SimpleOperation

          if (currentVariables.size < 3) {
            currentVariables.add(op.valueInfo!!)
          }
        }
        Opcode.OP_ASTORE_3 -> {
          stack.removeLast()

          val op = operations[i] as SimpleOperation

          if (currentVariables.size < 4) {
            currentVariables.add(op.valueInfo!!)
          }
        }
        Opcode.OP_ATHROW -> stack.clear()
        Opcode.OP_CHECKCAST -> {}

        Opcode.OP_DADD, Opcode.OP_DDIV, Opcode.OP_DMUL, Opcode.OP_DSUB -> {
          stack.removeLast()
          stack.removeLast()
        }

        Opcode.OP_DCMPG -> {
          stack.removeLast()
          stack.removeLast()
          stack.addLast(IntegerVti())
        }

        Opcode.OP_DLOAD_0, Opcode.OP_DLOAD_1 -> {
          stack.addLast(DoubleVti())
          stack.addLast(EmptyVti())
        }

        Opcode.OP_DNEG -> {}
        Opcode.OP_DUP -> stack.addLast(stack.last())

        Opcode.OP_GETFIELD -> {
          stack.removeLast()
          stack.addLast(operations[i].valueInfo!!)
        }

        Opcode.OP_GETSTATIC -> stack.addLast(operations[i].valueInfo!!)
        Opcode.OP_ICONST_0 -> stack.addLast(IntegerVti())
        Opcode.OP_IADD -> stack.removeLast()
        Opcode.OP_IAND -> stack.removeLast()
        Opcode.OP_ICONST_1 -> stack.addLast(IntegerVti())
        Opcode.OP_ICONST_2 -> stack.addLast(IntegerVti())
        Opcode.OP_ICONST_3 -> stack.addLast(IntegerVti())
        Opcode.OP_IFEQ, Opcode.OP_IFNE -> {
          stack.removeLast()
          // here we should set stack map frame at the destination where we are jumping
          val ind = (operations[i] as ControlFlowOperation).getJumpTo()
          println("    TEH JUMP to $ind")
          frames[ind].needToBuild(true)
          frames[ind].stack(stack.toList())
          frames[ind].locals(currentVariables.toList())
        }

        Opcode.OP_GOTO -> {
          val ind = (operations[i] as ControlFlowOperation).getJumpTo()
          println("    TEH GOTO JUMP to $ind")
          frames[ind].needToBuild(true)
          frames[ind].stack(stack.toList())
          frames[ind].locals(currentVariables.toList())
        }

        Opcode.OP_ILOAD_1 -> stack.addLast(IntegerVti())
        Opcode.OP_INSTANCEOF -> {
          stack.removeLast()
          stack.addLast(IntegerVti())
        }

        Opcode.OP_INVOKE_SPECIAL, Opcode.OP_INVOKE_STATIC, Opcode.OP_INVOKE_VIRTUAL -> {
          val mri = (operations[i] as ShortConstantOperation).constant as MethodRefInfo
          val args = mri.argsSize


          println("    Label: ${mri.label}")
          println("    Arguments Size: ${operations[i].opcode} $args")

          repeat(args) {
            stack.removeLast()
          }

          when (mri.returnTypeInfo) {
            is EmptyVti -> {}
            else -> stack.addLast(mri.returnTypeInfo)
          }
        }

        Opcode.OP_IRETURN -> stack.clear()
        Opcode.OP_ISHR -> stack.removeLast()
        Opcode.OP_IXOR -> stack.removeLast()
        Opcode.OP_LDC, Opcode.OP_LDC2_W, Opcode.OP_NEW -> {
          val vi = operations[i].valueInfo!!
          stack.addLast(vi)
          if (vi is DoubleVti) {
            stack.addLast(EmptyVti())
          }
        }

        Opcode.OP_POP -> stack.removeLast()
        Opcode.OP_PUTFIELD -> {
          stack.removeLast() // can fuck up with doubles
          stack.removeLast()
        }

        Opcode.OP_PUTSTATIC -> stack.removeLast() // can fuck up with doubles
        Opcode.OP_RETURN -> stack.clear()
        Opcode.OP_ASTORE -> {
          stack.removeLast()
          val op = operations[i] as OperationWithIndex

          if (currentVariables.size <= op.index.toInt()) {
            currentVariables.add(op.valueInfo!!)
            print("    CurrentVarsSize: ${currentVariables.size}")
            print("    ASTORE INDEX:    ${op.index.toInt()}")
          }
        }

        Opcode.OP_IF_ICMPNE, Opcode.OP_IF_ICMPEQ -> {
          if (stack.size < 2) {
            throw IllegalStateException(
              "To operate, opcode ${operations[i].opcode} should have at least two operations on the stack.")
          }
          stack.removeLast()
          stack.removeLast()

          val ind = (operations[i] as ControlFlowOperation).getJumpTo()
          println("    TEH JUMP to $ind")
          frames[ind].needToBuild(true)
          frames[ind].stack(stack.toList())
          frames[ind].locals(currentVariables.toList())
        }

        Opcode.OP_L2D -> {
          stack.removeLast()
          stack.addLast(DoubleVti())
        }

        Opcode.OP_SWAP -> {
          val top = stack.removeLast()
          val prev = stack.removeLast()
          stack.addLast(top)
          stack.addLast(prev)
        }

        Opcode.OP_NOP -> {}
        Opcode.OP_DUP_2 -> {
          val top = stack.removeLast()
          val prev = stack.removeLast()
          stack.addLast(prev)
          stack.addLast(top)
          stack.addLast(prev)
          stack.addLast(top)
        }
      }

      print("    Stack: ")
      for (s in stack) {
        print("$s, ")
      }
      println()
      maxStackSize = maxOf(maxStackSize, stack.size)
    }

    return StackMapTableAttribute(frames = makeFrames(frames, offsets))
  }

  private fun makeFrames(frameBuilders: List<FullFrameBuilder>, offsets: List<Int>): List<FullFrame> {
    val res = mutableListOf<FullFrame>()
    var lastOffset = 0

    var one = 0
    for ((i, fb) in frameBuilders.withIndex()) {
      if (fb.toBuild) {
        val frameOffset = offsets[i] - lastOffset - one
        res.add(fb.offsetDelta(frameOffset.toShort()).build())
        lastOffset = offsets[i]
        one = 1
      }
    }

    return res
  }

  private fun calculateOffsets(operations: List<Operation>): List<Int> {
    val res = mutableListOf<Int>()
    var offset = 0

    for (op in operations) {
      res.add(offset)

      offset += when (op) {
        is ControlFlowOperation -> 3
        is ByteConstantOperation -> 2
        is ShortConstantOperation -> 3
        is SimpleOperation -> 1
        is OperationWithIndex -> 2
      }
    }

    return res
  }

  private fun composeArrayOfLocalVariables(
    methodInfo: MethodInfo,
    thisClassInfo: ClassInfo
  ): List<VerificationTypeInfo> {
    val res = mutableListOf<VerificationTypeInfo>()

    if (!methodInfo.isStatic) {
      res.add(ObjectVti(thisClassInfo))
    }

    for (arg in methodInfo.signature.arguments) {
      res.add(arg)

      if (arg is DoubleVti) {
        res.add(EmptyVti())
      }
    }

    // For now this should do the trick.
    // Later, when I add local variables, I'll add more

    // Nope, it didn't cut it.

    // Actually, it did. Array of local variables actually changes during runtime.

    return res.toList()
  }
}
