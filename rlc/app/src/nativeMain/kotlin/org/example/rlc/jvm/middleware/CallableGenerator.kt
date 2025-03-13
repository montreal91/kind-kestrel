package org.example.rlc.jvm.middleware

import org.example.rlc.jvm.ir.ByteConstantOperation
import org.example.rlc.jvm.ir.ClassAccessFlags
import org.example.rlc.jvm.ir.ClassFile
import org.example.rlc.jvm.ir.ClassInfo
import org.example.rlc.jvm.ir.CodeAttribute
import org.example.rlc.jvm.ir.EmptyVti
import org.example.rlc.jvm.ir.MethodAccessFlags
import org.example.rlc.jvm.ir.MethodInfo
import org.example.rlc.jvm.ir.MethodRefInfo
import org.example.rlc.jvm.ir.MethodSignature
import org.example.rlc.jvm.ir.ObjectVti
import org.example.rlc.jvm.ir.Opcode
import org.example.rlc.jvm.ir.Operation
import org.example.rlc.jvm.ir.ShortConstantOperation
import org.example.rlc.jvm.ir.SimpleOperation
import org.example.rlc.jvm.ir.StringRefInfo
import org.example.rlc.jvm.ir.VerificationTypeInfo
import org.example.rlc.jvm.ir.javaLangStringObjectVti
import org.example.rlc.jvm.ir.loxDouble
import org.example.rlc.jvm.ir.loxFunction

private val loxObjectVti = ObjectVti(loxObjectClassInfo)

internal fun generateLoxFunction(name: String, arity: Int, code: List<Operation>): ClassFile {
  val codeAttribute = CodeAttribute(
    argsSize = arity + 1,
    code = code
  )

  val callMethod = MethodInfo(
    methodName = "__call__",
    accessFlagList = listOf(),
    attributeList = listOf(codeAttribute),
    signature = MethodSignature(
      arguments = generateMethodSignature(arity),
      returnType = loxObjectVti
    ),
    isStatic = false
  )

  return ClassFile(
    thisClassInfo = ClassInfo(className = "LoxFunction${name}"),
    superClassInfo = ClassInfo(className = "LoxCallable${arity}"),
    interfaceList = listOf(),
    methodList = listOf(
      generateConstructor(className = "LoxCallable${arity}"),
      callMethod,
      generateToString(name)
    ),
    attributeList = listOf(),
    accessFlagList = listOf(),
    fieldList = listOf()
  )
}

internal fun generateAbstractCallable(arity: Int): ClassFile {
  val callMethod = MethodInfo(
    methodName = "__call__",
    accessFlagList = listOf(MethodAccessFlags.ABSTRACT),
    attributeList = listOf(),
    signature = MethodSignature(
      arguments = generateMethodSignature(arity),
      returnType = loxObjectVti
    ),
    isStatic = false
  )

  return ClassFile(
    thisClassInfo = ClassInfo(className = "LoxCallable${arity}"),
    superClassInfo = ClassInfo(className = "LoxBasicCallable"),
    interfaceList = listOf(),
    methodList = listOf(generateConstructor(className = "LoxBasicCallable"), callMethod),
    attributeList = listOf(),
    accessFlagList = listOf(ClassAccessFlags.ABSTRACT),
    fieldList = listOf()
  )
}

internal fun generateLoxBasicCallable(): ClassFile {
  val code = listOf(
    SimpleOperation(Opcode.OP_ALOAD_0),
    ShortConstantOperation(
      Opcode.OP_GETSTATIC,
      generateLoxClassFieldRef(fieldName = "LOX_FUNCTION_CLASS"),
      loxObjectVti
    ),
    ShortConstantOperation(Opcode.OP_INVOKE_SPECIAL, loxObjectConstructor),
    SimpleOperation(Opcode.OP_RETURN)
  )

  val loxCallableConstructor = MethodInfo(
    methodName = "<init>",
    accessFlagList = listOf(),
    attributeList = listOf(CodeAttribute(code = code, argsSize = 1)),
    isStatic = false,
    signature = MethodSignature(listOf(), EmptyVti()),
  )

  return ClassFile(
    thisClassInfo = ClassInfo(className = "LoxBasicCallable"),
    superClassInfo = loxObjectClassInfo,
    interfaceList = listOf(),
    methodList = listOf(loxCallableConstructor),
    accessFlagList = listOf(ClassAccessFlags.ABSTRACT),
    attributeList = listOf(),
    fieldList = listOf()
  )
}

private fun generateMethodSignature(arity: Int): List<VerificationTypeInfo> =
  List(arity) { loxObjectVti }

private fun generateToString(name: String): MethodInfo {
  val codeAttribute = CodeAttribute(
    code = listOf(
      ByteConstantOperation(Opcode.OP_LDC, StringRefInfo(value = "<fn $name>"), javaLangStringObjectVti),
      SimpleOperation(Opcode.OP_ARETURN),
    ),
    argsSize = 1
  )

  return MethodInfo(
    methodName = "toString",
    accessFlagList = listOf(MethodAccessFlags.PUBLIC),
    attributeList = listOf(codeAttribute),
    isStatic = false,
    signature = MethodSignature(listOf(), ObjectVti(javaLangStringClassInfo))
  )
}

private fun generateConstructor(className: String): MethodInfo {
  val code = listOf(
    SimpleOperation(Opcode.OP_ALOAD_0),
    ShortConstantOperation(Opcode.OP_INVOKE_SPECIAL, generateConstructorMethodRef(className)),
    SimpleOperation(Opcode.OP_RETURN)
  )

  return MethodInfo(
    methodName = "<init>",
    accessFlagList = listOf(),
    attributeList = listOf(CodeAttribute(code = code, argsSize = 1)),
    isStatic = false,
    signature = MethodSignature(listOf(), EmptyVti())
  )
}

private fun generateConstructorMethodRef(className: String): MethodRefInfo {
  return MethodRefInfo(
    label = "$className.\"<init>\":()V",
    classInfo = ClassInfo(className),
    nameAndType = initializerNameAndType, // Maybe this also will require some parametrization
    argsSize = 1,
    returnSize = 0,
    returnTypeInfo = EmptyVti(),
  )
}
