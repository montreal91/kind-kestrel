package org.example.rlc.jvm.middleware

import org.example.rlc.jvm.ir.ByteConstantOperation
import org.example.rlc.jvm.ir.ClassFile
import org.example.rlc.jvm.ir.ClassInfo
import org.example.rlc.jvm.ir.CodeAttribute
import org.example.rlc.jvm.ir.EmptyVti
import org.example.rlc.jvm.ir.MethodAccessFlags
import org.example.rlc.jvm.ir.MethodInfo
import org.example.rlc.jvm.ir.MethodSignature
import org.example.rlc.jvm.ir.ObjectVti
import org.example.rlc.jvm.ir.Opcode
import org.example.rlc.jvm.ir.ShortConstantOperation
import org.example.rlc.jvm.ir.SimpleOperation
import org.example.rlc.jvm.ir.StringRefInfo
import org.example.rlc.jvm.ir.javaLangStringObjectVti

internal fun generateConstructorClass(name: String): ClassFile {
  return ClassFile(
    thisClassInfo = ClassInfo(className = "LoxClass_$name"),
    superClassInfo = ClassInfo(className = "LoxCallable0"),
    accessFlagList = listOf(),
    interfaceList = listOf(),
    fieldList = listOf(),
    methodList = listOf(
      generateConstructor(className = "LoxCallable0"),
      generateConstructorCallMethod(className = name),
      generateToStringMethod(output = "<class $name>"),
      generateArity(arity = 0),
    ),
    attributeList = listOf(),
  )
}

internal fun generateInstanceClass(name: String): ClassFile {
  return ClassFile(
    thisClassInfo = ClassInfo(className = "LoxInstance_$name"),
    superClassInfo = ClassInfo(className = "LoxObject"), // Instance or object, that's the question.
    accessFlagList = listOf(),
    interfaceList = listOf(),
    fieldList = listOf(),
    methodList = listOf(
      generateConstructor(className = "LoxObject"),
      generateToStringMethod(output = "$name instance"),
    ),
    attributeList = listOf(),
  )
}

private fun generateToStringMethod(output: String): MethodInfo {
  val code = listOf(
    ByteConstantOperation(
      opcode = Opcode.OP_LDC,
      constant = StringRefInfo(value = output),
      value = ObjectVti(javaLangStringClassInfo)
    ),
    SimpleOperation(Opcode.OP_ARETURN)
  )

  val codeAttribute = CodeAttribute(
    argsSize = 1,
    code = code,
    exceptionTable = 0,
    attributes = listOf(),
    maxLocals2 = 1
  )

  return MethodInfo(
    methodName = "toString",
    accessFlagList = listOf(MethodAccessFlags.PUBLIC),
    attributeList = listOf(codeAttribute),
    isStatic = true,
    signature = MethodSignature(listOf(), ObjectVti(javaLangStringClassInfo))
  )
}

private fun generateConstructorCallMethod(className: String): MethodInfo {
  val loxInstanceCi = ClassInfo("LoxInstance_$className")

  val codeAttribute = CodeAttribute(argsSize = 1, code = listOf(
    ShortConstantOperation(Opcode.OP_NEW, loxInstanceCi, value = ObjectVti(loxInstanceCi)),
    SimpleOperation(Opcode.OP_DUP),
    ShortConstantOperation(Opcode.OP_INVOKE_SPECIAL, generateConstructorMethodRef("LoxInstance_$className")),
    SimpleOperation(Opcode.OP_ARETURN),
  ))

  return MethodInfo(
    methodName = "__call__",
    accessFlagList = listOf(),
    attributeList = listOf(codeAttribute),
    signature = MethodSignature(
      arguments = generateMethodSignature(arity = 0),
      returnType = loxObjectVti
    ),
    isStatic = false
  )
}
