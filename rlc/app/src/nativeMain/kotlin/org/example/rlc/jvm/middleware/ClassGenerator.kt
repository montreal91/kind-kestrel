package org.example.rlc.jvm.middleware

import org.example.rlc.jvm.ir.ByteConstantOperation
import org.example.rlc.jvm.ir.ClassFile
import org.example.rlc.jvm.ir.ClassInfo
import org.example.rlc.jvm.ir.CodeAttribute
import org.example.rlc.jvm.ir.ControlFlowOperation
import org.example.rlc.jvm.ir.EmptyVti
import org.example.rlc.jvm.ir.FieldInfo
import org.example.rlc.jvm.ir.FieldRefInfo
import org.example.rlc.jvm.ir.MethodAccessFlags
import org.example.rlc.jvm.ir.MethodInfo
import org.example.rlc.jvm.ir.MethodRefInfo
import org.example.rlc.jvm.ir.MethodSignature
import org.example.rlc.jvm.ir.NameAndTypeInfo
import org.example.rlc.jvm.ir.ObjectVti
import org.example.rlc.jvm.ir.Opcode
import org.example.rlc.jvm.ir.ShortConstantOperation
import org.example.rlc.jvm.ir.SimpleOperation
import org.example.rlc.jvm.ir.StringRefInfo
import org.example.rlc.jvm.ir.toUtf8Value

internal fun generateConstructorClass(name: String): ClassFile {
  return ClassFile(
    thisClassInfo = ClassInfo(className = "LoxClass_$name"),
    superClassInfo = ClassInfo(className = "LoxCallable0"),
    accessFlagList = listOf(),
    interfaceList = listOf(),
    fieldList = listOf(),
    methodList = listOf(
      generateDefaultConstructor(className = "LoxCallable0"),
      generateConstructorCallMethod(className = name),
      generateToStringMethod(output = "<class $name>"),
      generateArity(arity = 0),
    ),
    attributeList = listOf(),
  )
}

internal fun generateInstanceClass(name: String): ClassFile {
  val instanceClassName = "LoxInstance_$name"

  return ClassFile(
    thisClassInfo = ClassInfo(className = instanceClassName),
    superClassInfo = ClassInfo(className = "LoxObject"), // Instance or object, that's the question.
    accessFlagList = listOf(),
    interfaceList = listOf(),
    fieldList = listOf(objectFieldMap()),
    methodList = listOf(
      generateInstanceConstructor(thisClassName = instanceClassName, superClassName = "LoxObject"),
      getFieldMethod(instanceClassName),
      setFieldMethod(instanceClassName),
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
      value = JavaString.VERIFICATION_TYPE
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
    signature = MethodSignature(arguments = listOf(), returnType = JavaString.VERIFICATION_TYPE)
  )
}

private fun generateConstructorCallMethod(className: String): MethodInfo {
  val loxInstanceCi = ClassInfo(className = "LoxInstance_$className")

  val codeAttribute = CodeAttribute(argsSize = 1, code = listOf(
    ShortConstantOperation(Opcode.OP_NEW, constant = loxInstanceCi, value = ObjectVti(classInfo = loxInstanceCi)),
    SimpleOperation(Opcode.OP_DUP),
    ShortConstantOperation(Opcode.OP_INVOKE_SPECIAL, constant = generateConstructorMethodRef(className = "LoxInstance_$className")),
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

private fun objectFieldMap() = FieldInfo(
  accessFlagList = listOf(),
  fieldName = "__fields__".toUtf8Value(),
  fieldDescriptor = "Ljava/util/HashMap;".toUtf8Value(),
)

private fun getFieldMethod(instanceClassName: String): MethodInfo {
  val code = listOf(
    SimpleOperation(Opcode.OP_ALOAD_0),
    ShortConstantOperation(Opcode.OP_GETFIELD, objectFieldMapReference(instanceClassName), JavaHashMap.VERIFICATION_TYPE),
    SimpleOperation(Opcode.OP_ALOAD_1),
    ShortConstantOperation(Opcode.OP_INVOKE_VIRTUAL, JavaHashMap.CONTAINS_KEY),
    ControlFlowOperation(Opcode.OP_IFEQ, jumpTo = 11),
    SimpleOperation(Opcode.OP_ALOAD_0),
    ShortConstantOperation(Opcode.OP_GETFIELD, objectFieldMapReference(instanceClassName), JavaHashMap.VERIFICATION_TYPE),
    SimpleOperation(Opcode.OP_ALOAD_1),
    ShortConstantOperation(Opcode.OP_INVOKE_VIRTUAL, JavaHashMap.GET),
    ShortConstantOperation(Opcode.OP_CHECKCAST, loxObjectClassInfo),
    SimpleOperation(Opcode.OP_ARETURN),
    ShortConstantOperation(Opcode.OP_NEW, loxRuntimeErrorClassInfo, ObjectVti(loxRuntimeErrorClassInfo)),
    SimpleOperation(Opcode.OP_DUP),
    SimpleOperation(Opcode.OP_ALOAD_2),
    ShortConstantOperation(Opcode.OP_INVOKE_SPECIAL, runtimeErrorConstructorRef()),
    SimpleOperation(Opcode.OP_ATHROW),
  )

  return MethodInfo(
    methodName = "__get__",
    accessFlagList = listOf(MethodAccessFlags.NONE),
    attributeList = listOf(CodeAttribute(argsSize = 3, code = code)),
    isStatic = false,
    signature = MethodSignature(
      arguments = listOf(JavaString.VERIFICATION_TYPE, JavaString.VERIFICATION_TYPE),
      returnType = loxObjectVti
    )
  )
}

internal fun getInstanceFieldMethodRef(): MethodRefInfo {
  return MethodRefInfo(
    label = "LoxObject.__get__:(Ljava/lang/String;Ljava/lang/String;)LLoxObject;",
    classInfo = loxObjectClassInfo,
    nameAndType = NameAndTypeInfo(
      label = "__get__:(Ljava/lang/String;Ljava/lang/String;)LLoxObject;",
      name = "__get__".toUtf8Value(),
      descriptor = "(Ljava/lang/String;Ljava/lang/String;)LLoxObject;".toUtf8Value(),
    ),
    argsSize = 3,
    returnSize = 1,
    returnTypeInfo = loxObjectVti,
  )
}

internal fun setInstanceFieldMethodRef(): MethodRefInfo {
  val signature = "(LLoxObject;Ljava/lang/String;)LLoxObject;"
  return MethodRefInfo(
    label = "LoxObject.__set__:$signature",
    classInfo = loxObjectClassInfo,
    nameAndType = NameAndTypeInfo(
      label = "__set__:$signature",
      name = "__set__".toUtf8Value(),
      descriptor = signature.toUtf8Value(),
    ),
    argsSize = 3,
    returnSize = 1,
    returnTypeInfo = loxObjectVti,
  )
}

private fun setFieldMethod(instanceClassName: String): MethodInfo {
  val code = listOf(
    SimpleOperation(Opcode.OP_ALOAD_0),
    ShortConstantOperation(Opcode.OP_GETFIELD, constant = objectFieldMapReference(instanceClassName), value = JavaHashMap.VERIFICATION_TYPE),
    SimpleOperation(Opcode.OP_ALOAD_2),
    SimpleOperation(Opcode.OP_ALOAD_1),
    ShortConstantOperation(Opcode.OP_INVOKE_VIRTUAL, constant = JavaHashMap.PUT),
    ShortConstantOperation(Opcode.OP_CHECKCAST, constant = loxObjectClassInfo),
    SimpleOperation(Opcode.OP_ARETURN),
  )

  val codeAttribute = CodeAttribute(
    argsSize = 3,
    code = code,
    exceptionTable = 0,
    attributes = listOf(),
    maxLocals2 = 2
  )

  val signature = MethodSignature(
    arguments = listOf(ObjectVti(loxObjectClassInfo), JavaString.VERIFICATION_TYPE),
    returnType = loxObjectVti,
  )

  return MethodInfo(
    methodName = "__set__",
    accessFlagList = listOf(MethodAccessFlags.NONE),
    attributeList = listOf(codeAttribute),
    isStatic = false,
    signature = signature
  )
}

private fun objectFieldMapReference(instanceClassName: String) = FieldRefInfo(
  label = "$instanceClassName.__fields__:Ljava/util/HashMap;",
  classInfo = ClassInfo(className = instanceClassName),
  nameAndType = NameAndTypeInfo(
    label = "__fields__:Ljava/util/HashMap;",
    name = "__fields__".toUtf8Value(),
    descriptor = "Ljava/util/HashMap;".toUtf8Value(),
  )
)

private fun generateInstanceConstructor(thisClassName: String, superClassName: String): MethodInfo {
  val code = listOf(
    SimpleOperation(Opcode.OP_ALOAD_0),
    ShortConstantOperation(Opcode.OP_INVOKE_SPECIAL, generateConstructorMethodRef(superClassName)),
    SimpleOperation(Opcode.OP_ALOAD_0),
    ShortConstantOperation(Opcode.OP_NEW, JavaHashMap.CLASS_INFO, JavaHashMap.VERIFICATION_TYPE),
    SimpleOperation(Opcode.OP_DUP),
    ShortConstantOperation(Opcode.OP_INVOKE_SPECIAL, JavaHashMap.CONSTRUCTOR),
    ShortConstantOperation(Opcode.OP_PUTFIELD, objectFieldMapReference(thisClassName)),
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
