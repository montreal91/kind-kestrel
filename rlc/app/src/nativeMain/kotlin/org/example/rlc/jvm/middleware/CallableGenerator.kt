package org.example.rlc.jvm.middleware

import org.example.rlc.frontend.scope.EnclosedVariable
import org.example.rlc.jvm.ir.ByteConstantOperation
import org.example.rlc.jvm.ir.ClassAccessFlags
import org.example.rlc.jvm.ir.ClassFile
import org.example.rlc.jvm.ir.ClassInfo
import org.example.rlc.jvm.ir.CodeAttribute
import org.example.rlc.jvm.ir.ControlFlowOperation
import org.example.rlc.jvm.ir.EmptyVti
import org.example.rlc.jvm.ir.FieldInfo
import org.example.rlc.jvm.ir.IntegerValue
import org.example.rlc.jvm.ir.IntegerVti
import org.example.rlc.jvm.ir.MethodAccessFlags
import org.example.rlc.jvm.ir.MethodInfo
import org.example.rlc.jvm.ir.MethodRefInfo
import org.example.rlc.jvm.ir.MethodSignature
import org.example.rlc.jvm.ir.NameAndTypeInfo
import org.example.rlc.jvm.ir.ObjectVti
import org.example.rlc.jvm.ir.Opcode
import org.example.rlc.jvm.ir.Operation
import org.example.rlc.jvm.ir.ShortConstantOperation
import org.example.rlc.jvm.ir.SimpleOperation
import org.example.rlc.jvm.ir.StringRefInfo
import org.example.rlc.jvm.ir.VerificationTypeInfo
import org.example.rlc.jvm.ir.javaLangStringObjectVti
import org.example.rlc.jvm.ir.toUtf8Value

internal fun generateLoxFunction(
  name: String,
  arity: Int,
  code: List<Operation>,
  enclosedVariables: List<EnclosedVariable>
): ClassFile {
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
      generateArity(arity),
      generateToString(name)
    ),
    attributeList = listOf(),
    accessFlagList = listOf(),
    fieldList = generateFieldsFromEnclosedVariableList(variables = enclosedVariables)
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

  val abstractArity = MethodInfo(
    methodName = "__arity__",
    accessFlagList = listOf(MethodAccessFlags.ABSTRACT),
    attributeList = emptyList(),
    isStatic = false,
    signature = MethodSignature(listOf(), IntegerVti())
  )

  return ClassFile(
    thisClassInfo = ClassInfo(className = "LoxBasicCallable"),
    superClassInfo = loxObjectClassInfo,
    interfaceList = listOf(),
    methodList = listOf(loxCallableConstructor, abstractArity, checkCallFunction()),
    accessFlagList = listOf(ClassAccessFlags.ABSTRACT),
    attributeList = listOf(),
    fieldList = listOf()
  )
}

internal fun generateMethodSignature(arity: Int): List<VerificationTypeInfo> =
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

internal fun generateArity(arity: Int): MethodInfo {
  val code = mutableListOf<Operation>()

  code.add(ByteConstantOperation(Opcode.OP_LDC, IntegerValue(arity), IntegerVti()))
  code.add(SimpleOperation(Opcode.OP_IRETURN))

  return MethodInfo(
    methodName = "__arity__",
    accessFlagList = listOf(),
    attributeList = listOf(CodeAttribute(code = code, argsSize = 1)),
    isStatic = false,
    signature = MethodSignature(listOf(), IntegerVti())
  )
}

private fun checkCallFunction(): MethodInfo {
  val code = mutableListOf<Operation>()

  code.add(SimpleOperation(Opcode.OP_ALOAD_0))
  code.add(ShortConstantOperation(Opcode.OP_INSTANCEOF, ClassInfo(className = "LoxBasicCallable")))

  val ifCallableJump = ControlFlowOperation(Opcode.OP_IFNE, jumpTo = -1)

  code.add(ifCallableJump)
  code.addAll(generateRuntimeError("Only functions and classes are callable."))

  ifCallableJump.setJumpTo(code.size)

  code.add(SimpleOperation(Opcode.OP_ALOAD_0))
  code.add(ShortConstantOperation(Opcode.OP_CHECKCAST, ClassInfo(className = "LoxBasicCallable")))
  code.add(ShortConstantOperation(Opcode.OP_INVOKE_VIRTUAL, generateArityMethodRef(className = "LoxBasicCallable")))
  code.add(SimpleOperation(Opcode.OP_ILOAD_1))

  val goodArityJump = ControlFlowOperation(Opcode.OP_IF_ICMPEQ, jumpTo = -1)

  code.add(goodArityJump)
  code.addAll(generateRuntimeError("Wrong argument count."))

  goodArityJump.setJumpTo(code.size)

  code.add(SimpleOperation(Opcode.OP_RETURN))

  return MethodInfo(
    methodName = "__call_check__",
    accessFlagList = listOf(MethodAccessFlags.STATIC),
    attributeList = listOf(CodeAttribute(code = code, argsSize = 2)),
    isStatic = true,
    signature = MethodSignature(listOf(ObjectVti(loxObjectClassInfo), IntegerVti()), EmptyVti())
  )
}

private fun generateArityMethodRef(className: String): MethodRefInfo {
  return MethodRefInfo(
    label = "$className.__arity__:()I",
    classInfo = ClassInfo(className),
    nameAndType = NameAndTypeInfo(
      label = "__arity__:()I",
      name = "__arity__".toUtf8Value(),
      descriptor = "()I".toUtf8Value()
    ),
    argsSize = 1,
    returnSize = 1,
    returnTypeInfo = IntegerVti(),
  )
}

private fun generateFieldsFromEnclosedVariableList(variables: List<EnclosedVariable>): List<FieldInfo> {
  val res = mutableListOf<FieldInfo>()

  for (variable in variables) {
    res.add(generateFieldFromEnclosedVariable(variable))
  }

  return res.toList()
}

private fun generateFieldFromEnclosedVariable(variable: EnclosedVariable) = FieldInfo(
  accessFlagList = listOf(),
  fieldName = "__enclosed_value__${variable.name}__".toUtf8Value(),
  fieldDescriptor = "LLoxObject;".toUtf8Value()
)
