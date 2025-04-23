package org.example.rlc.jvm.middleware

import org.example.rlc.frontend.EnclosedLocal
import org.example.rlc.frontend.EnclosedUpvalue
import org.example.rlc.frontend.scope.EnclosedVariable
import org.example.rlc.jvm.ir.ClassInfo
import org.example.rlc.jvm.ir.ControlFlowOperation
import org.example.rlc.jvm.ir.EmptyVti
import org.example.rlc.jvm.ir.MethodRefInfo
import org.example.rlc.jvm.ir.NameAndTypeInfo
import org.example.rlc.jvm.ir.ObjectVti
import org.example.rlc.jvm.ir.Opcode
import org.example.rlc.jvm.ir.Operation
import org.example.rlc.jvm.ir.OperationWithIndex
import org.example.rlc.jvm.ir.ShortConstantOperation
import org.example.rlc.jvm.ir.SimpleOperation
import org.example.rlc.jvm.ir.loxObjectClassName
import org.example.rlc.jvm.ir.toUtf8Value

internal object LoxFunction {
  internal fun generateConstructorInfo(className: String) = MethodRefInfo(
    label = "${className}.\"<init>\":()V",
    classInfo = ClassInfo(className),
    nameAndType = NameAndTypeInfo(
      label = "\"<init>\":()V",
      name = "<init>".toUtf8Value(),
      descriptor = "()V".toUtf8Value(),
    ),
    argsSize = 1,
    returnSize = 0,
    returnTypeInfo = EmptyVti(),
  )

  internal fun generateInstantiationCode(
    functionName: String,
    outerFunctionName: String,
    enclosedVariables: List<EnclosedVariable>,
    currentCodeOffset: Int,
  ): List<Operation> {
    val thisClassInfo = ClassInfo(className = functionName)
    val res = wrapLocalsIfNeeded(enclosedVariables, currentCodeOffset)

    val invokeFunctionObjectConstructorOp = ShortConstantOperation(
      Opcode.OP_INVOKE_SPECIAL,
      constant = generateConstructorInfo(className = functionName)
    )

    res.add(ShortConstantOperation(Opcode.OP_NEW, constant = thisClassInfo, value = ObjectVti(thisClassInfo)))
    res.add(SimpleOperation(Opcode.OP_DUP))
    res.add(invokeFunctionObjectConstructorOp)

    println("Compiling enclosed variables.")
    for (v in enclosedVariables) {
      res.add(SimpleOperation(Opcode.OP_DUP))
      println("Enclosed Variable: $v")

      val putFieldOp = ShortConstantOperation(
        Opcode.OP_PUTFIELD,
        constant = JavaClass.generateFieldRef(className = functionName, fieldName = "__enclosed_value__${v.name}__")
      )

      val accessOps = when (v.enclosedObject) {
        is EnclosedLocal -> compileEnclosedLocal(variable = v)
        is EnclosedUpvalue -> compileEnclosedUpvalue(variable = v, outerFunctionName)
      }

      res.addAll(elements = accessOps)
      res.add(putFieldOp)
    }

    println("Finished compiling enclosed variables.")

    return res.toList()
  }

  internal fun generateCallMethodRef(arity: Int): MethodRefInfo {
    val className = "LoxCallable$arity"
    val functionName = "__call__"

    val sb = StringBuilder()
    sb.append("(")

    (0..<arity).forEach { i ->
      sb.append("L$loxObjectClassName;")
    }

    sb.append(")L$loxObjectClassName;")
    val signature = sb.toString()
    return MethodRefInfo(
      label = "$className.$functionName:${signature}",
      classInfo = ClassInfo(className),
      nameAndType = NameAndTypeInfo(
        label = "$functionName:${signature}",
        name = functionName.toUtf8Value(),
        descriptor = signature.toUtf8Value(),
      ),
      argsSize = arity + 1,
      returnSize = 1,
      returnTypeInfo = ObjectVti(loxObjectClassInfo)
    )
  }
}


private fun wrapLocalsIfNeeded(
  enclosedVariables: List<EnclosedVariable>,
  currentCodeOffset: Int
): MutableList<Operation> {
  val res = mutableListOf<Operation>()

  for (v in enclosedVariables) {
    if (v.enclosedObject !is EnclosedLocal) {
      continue
    }

    val loadVariableOp = OperationWithIndex(Opcode.OP_ALOAD, v.actualIndex().toByte(), loxObjectVti)
    val checkIfPointerOp = ShortConstantOperation(Opcode.OP_INSTANCEOF, ClassInfo(className = "LoxPointer"))
    val jumpIfPointerOp = ControlFlowOperation(Opcode.OP_IFNE, jumpTo = -1)
    val newPointerOp = ShortConstantOperation(
      Opcode.OP_NEW,
      ClassInfo(className = "LoxPointer"),
      ObjectVti(ClassInfo(className = "LoxPointer"))
    )
    val invokePointerConstructorOp = ShortConstantOperation(
      Opcode.OP_INVOKE_SPECIAL,
      LoxFunction.generateConstructorInfo(className = "LoxPointer"),
    )

    val putIntoVariableField = ShortConstantOperation(
      Opcode.OP_PUTFIELD,
      JavaClass.generateFieldRef(className = "LoxPointer", fieldName = "__value__")
    )

    val storeVariableOp = OperationWithIndex(Opcode.OP_ASTORE, v.actualIndex().toByte(), loxObjectVti)

    res.add(loadVariableOp)
    res.add(checkIfPointerOp)
    res.add(jumpIfPointerOp)
    res.add(newPointerOp)
    res.add(SimpleOperation(Opcode.OP_DUP))
    res.add(SimpleOperation(Opcode.OP_DUP))
    res.add(invokePointerConstructorOp)
    res.add(loadVariableOp)
    res.add(putIntoVariableField)
    res.add(storeVariableOp)

    jumpIfPointerOp.setJumpTo(currentCodeOffset + res.size)
  }

  return res
}


private fun compileEnclosedLocal(variable: EnclosedVariable): List<Operation> {
  if (variable.enclosedObject !is EnclosedLocal) {
    throw IllegalStateException(
      message = "Trying to compile local, but it is not local.\nVariable=($variable)"
    )
  }

  val res = mutableListOf<Operation>()
  val loadFromLocalArrayOp = OperationWithIndex(
    Opcode.OP_ALOAD,
    index = variable.actualIndex().toByte(),
    value = ObjectVti(loxObjectClassInfo)
  )

  res.add(loadFromLocalArrayOp)

  return res.toList()
}

private fun compileEnclosedUpvalue(variable: EnclosedVariable, functionName: String): List<Operation> {
  if (variable.enclosedObject !is EnclosedUpvalue) {
    throw IllegalStateException("Trying to compile upvalue, but it is not upvalue.\nVariable=($variable)")
  }

  val res = mutableListOf<Operation>()
  val getFieldOp = ShortConstantOperation(
    Opcode.OP_GETFIELD,
    JavaClass.generateFieldRef(
      className = "LoxFunction$functionName",
      fieldName = "__enclosed_value__${variable.name}__", // this should be from outer function
    ),
    loxObjectVti,
  )

  res.add(SimpleOperation(Opcode.OP_ALOAD_0))
  res.add(getFieldOp)

  return res.toList()
}
