package org.example.rlc.jvm.middleware

import org.example.rlc.jvm.ir.BooleanVti
import org.example.rlc.jvm.ir.ByteConstantOperation
import org.example.rlc.jvm.ir.ClassInfo
import org.example.rlc.jvm.ir.CodeAttribute
import org.example.rlc.jvm.ir.ControlFlowOperation
import org.example.rlc.jvm.ir.EmptyVti
import org.example.rlc.jvm.ir.MethodAccessFlags
import org.example.rlc.jvm.ir.MethodInfo
import org.example.rlc.jvm.ir.MethodRefInfo
import org.example.rlc.jvm.ir.MethodSignature
import org.example.rlc.jvm.ir.NameAndTypeInfo
import org.example.rlc.jvm.ir.NullVariableVti
import org.example.rlc.jvm.ir.ObjectVti
import org.example.rlc.jvm.ir.Opcode
import org.example.rlc.jvm.ir.ShortConstantOperation
import org.example.rlc.jvm.ir.SimpleOperation
import org.example.rlc.jvm.ir.StringRefInfo
import org.example.rlc.jvm.ir.VerificationTypeInfo
import org.example.rlc.jvm.ir.javaLangStringObjectVti
import org.example.rlc.jvm.ir.toUtf8Value


internal val objectInitializer = "<init>".toUtf8Value()

internal fun runtimeErrorConstructorRef(): MethodRefInfo {
  return MethodRefInfo(
    label = "LoxRuntimeError.\"<init>\":(Ljava/lang/String;)V",
    classInfo = loxRuntimeErrorClassInfo,
    nameAndType = NameAndTypeInfo(
      label = "\"<init>\":(Ljava/lang/String;)V",
      name = objectInitializer,
      descriptor = "(Ljava/lang/String;)V".toUtf8Value()
    ),
    argsSize = 2,
    returnSize = 0,
    returnTypeInfo = EmptyVti()
  )
}

// Add method is a bit different because
// Lox supports concatenation of strings with + operator
internal fun addMethod(): MethodInfo {
  // To properly implement Lox runtime errors, this (and other) magic methods should be inlined.
  val doubleAddMagicMethod = MethodRefInfo(
    label = "LoxDouble.__add__:(LLoxDouble;)LLoxDouble;",
    classInfo = loxDoubleClassInfo,
    nameAndType = NameAndTypeInfo(
      label = "__add__:(LLoxDouble;)LLoxDouble;",
      name = "__add__".toUtf8Value(),
      descriptor = "(LLoxDouble;)LLoxDouble;".toUtf8Value()
    ),
    argsSize = 2,
    returnSize = 1,
    returnTypeInfo = ObjectVti(loxDoubleClassInfo, isArray = false),
  )

  val stringConcatMagicMethod = MethodRefInfo(
    label = "LoxString.__add__:(LLoxString;)LLoxString;",
    classInfo = loxStringClassInfo,
    nameAndType = NameAndTypeInfo(
      label = "__add__:(LLoxString;)LLoxString;",
      name = "__add__".toUtf8Value(),
      descriptor = "(LLoxString;)LLoxString;".toUtf8Value()
    ),
    argsSize = 2,
    returnSize = 1,
    returnTypeInfo = ObjectVti(loxStringClassInfo, isArray = false),
  )

  val errorMessage = "Both operands should be numbers or strings."

  // This code will change after a while
  val code = mutableListOf(
    SimpleOperation(Opcode.OP_ACONST_NULL),
    SimpleOperation(Opcode.OP_ASTORE_2, value = ObjectVti(loxDoubleClassInfo)),
    SimpleOperation(Opcode.OP_ACONST_NULL),
    SimpleOperation(Opcode.OP_ASTORE_3, value = ObjectVti(loxObjectClassInfo)),
    SimpleOperation(Opcode.OP_ALOAD_0),
    ShortConstantOperation(Opcode.OP_INSTANCEOF, loxDoubleClassInfo),
    // Hmm, maybe I need something like labels for these purposes
    ControlFlowOperation(Opcode.OP_IFEQ, jumpTo = 20),
    SimpleOperation(Opcode.OP_ALOAD_1),
    ShortConstantOperation(Opcode.OP_INSTANCEOF, loxDoubleClassInfo),
    ControlFlowOperation(Opcode.OP_IFEQ, jumpTo = 20),
    SimpleOperation(Opcode.OP_ALOAD_0),
    ShortConstantOperation(Opcode.OP_CHECKCAST, loxDoubleClassInfo),
    SimpleOperation(Opcode.OP_ASTORE_2, ObjectVti(loxDoubleClassInfo)),
    SimpleOperation(Opcode.OP_ALOAD_1),
    ShortConstantOperation(Opcode.OP_CHECKCAST, loxDoubleClassInfo),
    SimpleOperation(Opcode.OP_ASTORE_3, ObjectVti(loxDoubleClassInfo)),
    SimpleOperation(Opcode.OP_ALOAD_2),
    SimpleOperation(Opcode.OP_ALOAD_3),
    ShortConstantOperation(Opcode.OP_INVOKE_VIRTUAL, doubleAddMagicMethod),
    SimpleOperation(Opcode.OP_ARETURN),
    SimpleOperation(Opcode.OP_ALOAD_0),
    ShortConstantOperation(Opcode.OP_INSTANCEOF, loxStringClassInfo),
    ControlFlowOperation(Opcode.OP_IFEQ, jumpTo = 36),
    SimpleOperation(Opcode.OP_ALOAD_1),
    ShortConstantOperation(Opcode.OP_INSTANCEOF, loxStringClassInfo),
    ControlFlowOperation(Opcode.OP_IFEQ, jumpTo = 36),
    SimpleOperation(Opcode.OP_ALOAD_0),
    ShortConstantOperation(Opcode.OP_CHECKCAST, loxStringClassInfo),
    SimpleOperation(Opcode.OP_ASTORE_2, ObjectVti(loxStringClassInfo)),
    SimpleOperation(Opcode.OP_ALOAD_1),
    ShortConstantOperation(Opcode.OP_CHECKCAST, loxStringClassInfo),
    SimpleOperation(Opcode.OP_ASTORE_3, ObjectVti(loxStringClassInfo)),
    SimpleOperation(Opcode.OP_ALOAD_2),
    SimpleOperation(Opcode.OP_ALOAD_3),
    ShortConstantOperation(Opcode.OP_INVOKE_VIRTUAL, stringConcatMagicMethod),
    SimpleOperation(Opcode.OP_ARETURN),
  )

  code.addAll(generateRuntimeError(errorMessage))

  val codeAttribute = CodeAttribute(
    argsSize = 2,
    code = code.toList(),
  )

  return makeBinaryMethodInfo(
    methodName = "__add__",
    codeAttribute = codeAttribute,
    localVariables = listOf(
      ObjectVti(loxObjectClassInfo),
      ObjectVti(loxObjectClassInfo),
    )
  )
}

internal fun equalsMethod(): MethodInfo {
  val code = listOf(
    SimpleOperation(Opcode.OP_ACONST_NULL),
    SimpleOperation(Opcode.OP_ASTORE_2, value = NullVariableVti()),
    SimpleOperation(Opcode.OP_ACONST_NULL),
    SimpleOperation(Opcode.OP_ASTORE_3, value = NullVariableVti()),
    SimpleOperation(Opcode.OP_ALOAD_0),
    ShortConstantOperation(Opcode.OP_INVOKE_VIRTUAL, getClassMri),
    SimpleOperation(Opcode.OP_ALOAD_1),
    ShortConstantOperation(Opcode.OP_INVOKE_VIRTUAL, getClassMri),
    ShortConstantOperation(Opcode.OP_INVOKE_VIRTUAL, objectEqualsMri),
    ControlFlowOperation(Opcode.OP_IFNE, jumpTo = 15),
    ShortConstantOperation(Opcode.OP_NEW, loxBooleanClassInfo, ObjectVti(loxBooleanClassInfo)),
    SimpleOperation(Opcode.OP_DUP),
    SimpleOperation(Opcode.OP_ICONST_0),
    ShortConstantOperation(Opcode.OP_INVOKE_SPECIAL, loxBooleanConstructorInfo),
    SimpleOperation(Opcode.OP_ARETURN),
    SimpleOperation(Opcode.OP_ALOAD_0),
    ShortConstantOperation(Opcode.OP_INSTANCEOF, loxNilClassInfo),
    ControlFlowOperation(Opcode.OP_IFEQ, jumpTo = 23),
    ShortConstantOperation(Opcode.OP_NEW, loxBooleanClassInfo, ObjectVti(loxBooleanClassInfo)),
    SimpleOperation(Opcode.OP_DUP),
    SimpleOperation(Opcode.OP_ICONST_1),
    ShortConstantOperation(Opcode.OP_INVOKE_SPECIAL, loxBooleanConstructorInfo),
    SimpleOperation(Opcode.OP_ARETURN),
    SimpleOperation(Opcode.OP_ALOAD_0),
    SimpleOperation(Opcode.OP_ALOAD_1),
    ShortConstantOperation(Opcode.OP_INVOKE_VIRTUAL, loxObjectEqMri),
    SimpleOperation(Opcode.OP_ARETURN),
  )

  val codeAttribute = CodeAttribute(argsSize = 2, code = code)

  return makeBinaryMethodInfo(methodName = "__eq__", codeAttribute = codeAttribute, listOf())
}

internal fun numberMagicMethod(methodName: String, returnType: String): MethodInfo {
  val methodRefInfo = MethodRefInfo(
    label = "LoxDouble.${methodName}:(LLoxDouble;)L$returnType;",
    classInfo = loxDoubleClassInfo,
    nameAndType = NameAndTypeInfo(
      label = "${methodName}:(LLoxDouble;)L$returnType;",
      name = methodName.toUtf8Value(),
      descriptor = "(LLoxDouble;)L$returnType;".toUtf8Value()
    ),
    argsSize = 2,
    returnSize = 1,
    returnTypeInfo = ObjectVti(ClassInfo(returnType), isArray = false)
  )

  val code = listOf(
    SimpleOperation(Opcode.OP_ACONST_NULL),
    SimpleOperation(Opcode.OP_ASTORE_2, value = NullVariableVti()),
    SimpleOperation(Opcode.OP_ACONST_NULL),
    SimpleOperation(Opcode.OP_ASTORE_3, value = NullVariableVti()),
    SimpleOperation(Opcode.OP_ALOAD_0),
    ShortConstantOperation(Opcode.OP_INSTANCEOF, loxDoubleClassInfo),
    ControlFlowOperation(Opcode.OP_IFEQ, jumpTo = 20),
    SimpleOperation(Opcode.OP_ALOAD_1),
    ShortConstantOperation(Opcode.OP_INSTANCEOF, loxDoubleClassInfo),
    ControlFlowOperation(Opcode.OP_IFEQ, jumpTo = 20),
    SimpleOperation(Opcode.OP_ALOAD_0),
    ShortConstantOperation(Opcode.OP_CHECKCAST, loxDoubleClassInfo),
    SimpleOperation(Opcode.OP_ASTORE_2, ObjectVti(loxDoubleClassInfo)),
    SimpleOperation(Opcode.OP_ALOAD_1),
    ShortConstantOperation(Opcode.OP_CHECKCAST, loxDoubleClassInfo),
    SimpleOperation(Opcode.OP_ASTORE_3, ObjectVti(loxDoubleClassInfo)),
    SimpleOperation(Opcode.OP_ALOAD_2),
    SimpleOperation(Opcode.OP_ALOAD_3),
    ShortConstantOperation(Opcode.OP_INVOKE_VIRTUAL, methodRefInfo),
    SimpleOperation(Opcode.OP_ARETURN),
    ShortConstantOperation(Opcode.OP_NEW, loxRuntimeErrorClassInfo, ObjectVti(loxRuntimeErrorClassInfo)),
    SimpleOperation(Opcode.OP_DUP),
    ByteConstantOperation(Opcode.OP_LDC, StringRefInfo(value = "Operands must be numbers."), javaLangStringObjectVti),
    ShortConstantOperation(Opcode.OP_INVOKE_SPECIAL, runtimeErrorConstructorRef()),
    SimpleOperation(Opcode.OP_ATHROW),
  )

  val codeAttribute = CodeAttribute(argsSize = 2, code = code)

  return makeBinaryMethodInfo(methodName, codeAttribute, listOf(ObjectVti(loxDoubleClassInfo), ObjectVti(
    loxDoubleClassInfo)
  ))
}

internal fun notEqualsMethod(): MethodInfo {
  val code = listOf(
    SimpleOperation(Opcode.OP_ALOAD_0),
    ShortConstantOperation(Opcode.OP_INVOKE_VIRTUAL, getClassMri),
    SimpleOperation(Opcode.OP_ALOAD_1),
    ShortConstantOperation(Opcode.OP_INVOKE_VIRTUAL, getClassMri),
    ShortConstantOperation(Opcode.OP_INVOKE_VIRTUAL, objectEqualsMri),
    ControlFlowOperation(Opcode.OP_IFNE, jumpTo = 11),
    ShortConstantOperation(Opcode.OP_NEW, loxBooleanClassInfo, ObjectVti(loxBooleanClassInfo)),
    SimpleOperation(Opcode.OP_DUP),
    SimpleOperation(Opcode.OP_ICONST_1),
    ShortConstantOperation(Opcode.OP_INVOKE_SPECIAL, loxBooleanConstructorInfo),
    SimpleOperation(Opcode.OP_ARETURN),
    SimpleOperation(Opcode.OP_ALOAD_0),
    ShortConstantOperation(Opcode.OP_INSTANCEOF, loxNilClassInfo),
    ControlFlowOperation(Opcode.OP_IFEQ, jumpTo = 19),
    ShortConstantOperation(Opcode.OP_NEW, loxBooleanClassInfo, ObjectVti(loxBooleanClassInfo)),
    SimpleOperation(Opcode.OP_DUP),
    SimpleOperation(Opcode.OP_ICONST_0),
    ShortConstantOperation(Opcode.OP_INVOKE_SPECIAL, loxBooleanConstructorInfo),
    SimpleOperation(Opcode.OP_ARETURN),
    SimpleOperation(Opcode.OP_ALOAD_0),
    SimpleOperation(Opcode.OP_ALOAD_1),
    ShortConstantOperation(Opcode.OP_INVOKE_VIRTUAL, loxObjectEqMri),
    ShortConstantOperation(Opcode.OP_CHECKCAST, loxBooleanClassInfo),
    ShortConstantOperation(Opcode.OP_INVOKE_VIRTUAL, loxBooleanNegMri()),
    SimpleOperation(Opcode.OP_ARETURN),
  )

  val codeAttribute = CodeAttribute(argsSize = 2, code)

  return makeBinaryMethodInfo(methodName = "__neq__", codeAttribute = codeAttribute, listOf())
}

private fun makeBinaryMethodInfo(
  methodName: String,
  codeAttribute: CodeAttribute,
  localVariables: List<VerificationTypeInfo>
) = MethodInfo(
  methodName = methodName,
  accessFlagList = listOf(
    MethodAccessFlags.STATIC,
    MethodAccessFlags.FINAL,
  ),
  attributeList = listOf(codeAttribute),
  isStatic = true,
  signature = MethodSignature(
    listOf(ObjectVti(loxObjectClassInfo), ObjectVti(loxObjectClassInfo)),
    ObjectVti(loxObjectClassInfo)
  ),
  localVariables = localVariables
)

private fun makeUnaryMethodInfo(methodName: String, codeAttribute: CodeAttribute) = MethodInfo(
  methodName = methodName,
  accessFlagList = listOf(
    MethodAccessFlags.STATIC,
    MethodAccessFlags.FINAL,
  ),
  attributeList = listOf(codeAttribute),
  isStatic = true,
  signature = MethodSignature(listOf(ObjectVti(loxObjectClassInfo)), ObjectVti(loxObjectClassInfo))
)

internal fun unaryMinusMagicMethod(methodName: String): MethodInfo {
  val methodRefInfo = MethodRefInfo(
    label = "LoxDouble.${methodName}:()LLoxDouble;",
    classInfo = loxDoubleClassInfo,
    nameAndType = NameAndTypeInfo(
      label = "${methodName}:()LLoxDouble;",
      name = methodName.toUtf8Value(),
      descriptor = "()LLoxDouble;".toUtf8Value()
    ),
    argsSize = 1,
    returnSize = 1,
    returnTypeInfo = ObjectVti(loxDoubleClassInfo, isArray = false),
  )

  val code = listOf(
    SimpleOperation(Opcode.OP_ALOAD_0),
    ShortConstantOperation(Opcode.OP_INSTANCEOF, loxDoubleClassInfo),
    ControlFlowOperation(Opcode.OP_IFEQ, jumpTo = 7),
    SimpleOperation(Opcode.OP_ALOAD_0),
    ShortConstantOperation(Opcode.OP_CHECKCAST, loxDoubleClassInfo),
    ShortConstantOperation(Opcode.OP_INVOKE_VIRTUAL, methodRefInfo),
    SimpleOperation(Opcode.OP_ARETURN),
  )

  val codeAttribute = CodeAttribute(
    argsSize = 1,
    code = code + generateRuntimeError("Operand should be a number."),
  )

  return MethodInfo(
    methodName = methodName,
    accessFlagList = listOf(
      MethodAccessFlags.STATIC,
      MethodAccessFlags.FINAL,
      MethodAccessFlags.PRIVATE
    ),
    attributeList = listOf(codeAttribute),
    isStatic = true,
    signature = MethodSignature(listOf(ObjectVti(loxObjectClassInfo)), ObjectVti(loxObjectClassInfo))
  )
}

internal fun notOperatorMagicMethod(): MethodInfo {
  val methodName = "__truthy__"
  val objectTruthy = MethodRefInfo(
    label = "LoxObject.$methodName:()LLoxObject;",
    classInfo = loxObjectClassInfo,
    nameAndType = NameAndTypeInfo(
      label = "$methodName:()LLoxObject;",
      name = methodName.toUtf8Value(),
      descriptor = "()LLoxObject;".toUtf8Value()
    ),
    argsSize = 1,
    returnSize = 1,
    returnTypeInfo = ObjectVti(loxObjectClassInfo, isArray = false)
  )

  val code = listOf(
    SimpleOperation(Opcode.OP_ALOAD_0),
    ShortConstantOperation(Opcode.OP_INVOKE_VIRTUAL, objectTruthy),
    ShortConstantOperation(Opcode.OP_CHECKCAST, loxBooleanClassInfo),
    ShortConstantOperation(Opcode.OP_INVOKE_VIRTUAL, loxBooleanNegMri()),
    SimpleOperation(Opcode.OP_ARETURN),
  )

  val codeAttribute = CodeAttribute(
    argsSize = 1,
    code = code,
  )

  return makeUnaryMethodInfo(methodName = "__not__", codeAttribute = codeAttribute)
}

internal fun generateRuntimeError(message: String) = listOf(
  ShortConstantOperation(Opcode.OP_NEW, loxRuntimeErrorClassInfo, ObjectVti(loxRuntimeErrorClassInfo)),
  SimpleOperation(Opcode.OP_DUP),
  ByteConstantOperation(Opcode.OP_LDC, StringRefInfo(message), javaLangStringObjectVti),
  ShortConstantOperation(Opcode.OP_INVOKE_SPECIAL, runtimeErrorConstructorRef()),
  SimpleOperation(Opcode.OP_ATHROW),
)

internal fun setGlobalVariableMethod(): MethodInfo {
  val containsKeySignature = "(Ljava/lang/Object;)Z"
  val containsKeyMethodRef = MethodRefInfo(
    label = "java/lang/HashMap.containsKey:$containsKeySignature",
    classInfo = hashMapInfo,
    nameAndType = NameAndTypeInfo(
      label = "containsKey:$containsKeySignature",
      name = "containsKey".toUtf8Value(),
      descriptor = containsKeySignature.toUtf8Value(),
    ),
    argsSize = 2,
    returnSize = 1,
    returnTypeInfo = BooleanVti()
  )

  val code = listOf(
    ShortConstantOperation(Opcode.OP_GETSTATIC, drtReference(), ObjectVti(hashMapInfo)),
    SimpleOperation(Opcode.OP_ALOAD_1),
    ShortConstantOperation(Opcode.OP_INVOKE_VIRTUAL, containsKeyMethodRef),
    ControlFlowOperation(Opcode.OP_IFEQ, jumpTo = 9),
    ShortConstantOperation(Opcode.OP_GETSTATIC, drtReference(), ObjectVti(hashMapInfo)),
    SimpleOperation(Opcode.OP_ALOAD_1),
    SimpleOperation(Opcode.OP_ALOAD_0),
    ShortConstantOperation(Opcode.OP_INVOKE_VIRTUAL, putMethodRef),
    SimpleOperation(Opcode.OP_RETURN),
    ShortConstantOperation(Opcode.OP_NEW, loxRuntimeErrorClassInfo, ObjectVti(loxRuntimeErrorClassInfo)),
    SimpleOperation(Opcode.OP_DUP),
    SimpleOperation(Opcode.OP_ALOAD_2),
    ShortConstantOperation(Opcode.OP_INVOKE_SPECIAL, runtimeErrorConstructorRef()),
    SimpleOperation(Opcode.OP_ATHROW),
  )

  val codeAttribute = CodeAttribute(
    argsSize = 3,
    code = code,
    exceptionTable = 0,
    attributes = listOf(),
    maxLocals2 = 3
  )

  val signature = MethodSignature(
    listOf(ObjectVti(loxObjectClassInfo), ObjectVti(javaLangStringClassInfo), ObjectVti(javaLangStringClassInfo)),
    EmptyVti(),
  )

  return MethodInfo(
    methodName = "__set_global__",
    accessFlagList = listOf(MethodAccessFlags.STATIC),
    attributeList = listOf(codeAttribute),
    isStatic = true,
    signature = signature
  )
}

internal fun loxScriptStaticInitializer(): MethodInfo {
  val hashMapConstructor = MethodRefInfo(
    label = "java/util/HashMap.\"<init>\":()V",
    classInfo = hashMapInfo,
    nameAndType = NameAndTypeInfo(
      label = "\"<init>\":()V",
      name = "<init>".toUtf8Value(),
      descriptor = "()V".toUtf8Value(),
    ),
    argsSize = 2,
    returnSize = 0,
    returnTypeInfo = EmptyVti()
  )

  val code = listOf(
    ShortConstantOperation(Opcode.OP_NEW, hashMapInfo, ObjectVti(hashMapInfo)),
    SimpleOperation(Opcode.OP_DUP),
    ShortConstantOperation(Opcode.OP_INVOKE_SPECIAL, hashMapConstructor),
    ShortConstantOperation(Opcode.OP_PUTSTATIC, drtReference()),
    SimpleOperation(Opcode.OP_RETURN),
  )

  val codeAttribute = CodeAttribute(
    argsSize = 2,
    code = code,
    exceptionTable = 0,
    attributes = listOf(),
    maxLocals2 = 2
  )

  return MethodInfo(
    methodName = staticInitializerMethodName,
    accessFlagList = listOf(MethodAccessFlags.STATIC),
    attributeList = listOf(codeAttribute),
    isStatic = true,
    signature = MethodSignature(listOf(), EmptyVti())
  )
}

internal fun getGlobalVariableMethod(): MethodInfo {
  val containsKeySignature = "(Ljava/lang/Object;)Z"
  val containsKeyMethodRef = MethodRefInfo(
    label = "java/lang/HashMap.containsKey:$containsKeySignature",
    classInfo = hashMapInfo,
    nameAndType = NameAndTypeInfo(
      label = "containsKey:$containsKeySignature",
      name = "containsKey".toUtf8Value(),
      descriptor = containsKeySignature.toUtf8Value(),
    ),
    argsSize = 2,
    returnSize = 1,
    returnTypeInfo = BooleanVti()
  )

  val mapGetSignature = "(Ljava/lang/Object;)Ljava/lang/Object;"
  val mapGetMethodRef = MethodRefInfo(
    label = "java/lang/HashMap.get:$mapGetSignature",
    classInfo = hashMapInfo,
    nameAndType = NameAndTypeInfo(
      label = "get:$mapGetSignature",
      name = "get".toUtf8Value(),
      descriptor = mapGetSignature.toUtf8Value(),
    ),
    argsSize = 2,
    returnSize = 1,
    returnTypeInfo = BooleanVti()
  )

  val code = listOf(
    ShortConstantOperation(Opcode.OP_GETSTATIC, drtReference(), ObjectVti(hashMapInfo)),
    SimpleOperation(Opcode.OP_ALOAD_0),
    ShortConstantOperation(Opcode.OP_INVOKE_VIRTUAL, containsKeyMethodRef),
    ControlFlowOperation(Opcode.OP_IFEQ, jumpTo = 9),
    ShortConstantOperation(Opcode.OP_GETSTATIC, drtReference(), ObjectVti(hashMapInfo)),
    SimpleOperation(Opcode.OP_ALOAD_0),
    ShortConstantOperation(Opcode.OP_INVOKE_VIRTUAL, mapGetMethodRef),
    ShortConstantOperation(Opcode.OP_CHECKCAST, loxObjectClassInfo),
    SimpleOperation(Opcode.OP_ARETURN),
    ShortConstantOperation(Opcode.OP_NEW, loxRuntimeErrorClassInfo, ObjectVti(loxRuntimeErrorClassInfo)),
    SimpleOperation(Opcode.OP_DUP),
    SimpleOperation(Opcode.OP_ALOAD_1),
    ShortConstantOperation(Opcode.OP_INVOKE_SPECIAL, runtimeErrorConstructorRef()),
    SimpleOperation(Opcode.OP_ATHROW),
  )

  val codeAttribute = CodeAttribute(
    argsSize = 2,
    code = code,
    exceptionTable = 0,
    attributes = listOf(),
    maxLocals2 = 2
  )

  val signature = MethodSignature(
    listOf(ObjectVti(javaLangStringClassInfo), ObjectVti(javaLangStringClassInfo)),
    ObjectVti(loxObjectClassInfo)
  )

  return MethodInfo(
    methodName = "__get_global__",
    accessFlagList = listOf(MethodAccessFlags.STATIC),
    attributeList = listOf(codeAttribute),
    isStatic = true,
    signature = signature
  )
}

internal fun declGlobalVariableMethod(): MethodInfo {
  val code = listOf(
    ShortConstantOperation(Opcode.OP_GETSTATIC, drtReference(), ObjectVti(hashMapInfo)),
    SimpleOperation(Opcode.OP_ALOAD_1),
    SimpleOperation(Opcode.OP_ALOAD_0),
    ShortConstantOperation(Opcode.OP_INVOKE_VIRTUAL, putMethodRef),
    SimpleOperation(Opcode.OP_POP),
    SimpleOperation(Opcode.OP_RETURN),
  )

  val codeAttribute = CodeAttribute(
    argsSize = 2,
    code = code,
    exceptionTable = 0,
    attributes = listOf(),
    maxLocals2 = 2
  )

  val signature = MethodSignature(
    listOf(ObjectVti(loxObjectClassInfo), ObjectVti(javaLangStringClassInfo)),
    EmptyVti()
  )

  return MethodInfo(
    methodName = "__decl_global__",
    accessFlagList = listOf(MethodAccessFlags.STATIC),
    attributeList = listOf(codeAttribute),
    isStatic = true,
    signature = signature
  )
}
