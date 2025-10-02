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
import org.example.rlc.jvm.ir.OperationWithIndex
import org.example.rlc.jvm.ir.ShortConstantOperation
import org.example.rlc.jvm.ir.SimpleOperation
import org.example.rlc.jvm.ir.StringRefInfo
import org.example.rlc.jvm.ir.toUtf8Value

/**
 * Generates a class file for a constructor class.
 * The generated class inherits from a parent callable class with the specified arity.
 * It creates methods such as the default constructor, a callable constructor,
 * a string representation method, and an arity method.
 *
 * @param name the name of the constructor class to be generated
 * @param arity the arity (number of arguments) of the callable constructor
 * @param functionStuff a list of function metadata required for generating constructor logic
 * @return a ClassFile instance representing the generated constructor class
 */
internal fun generateConstructorClass(
  name: String,
  arity: Int,
  functionStuff: List<FunctionStuff>
): ClassFile {
  return ClassFile(
    thisClassInfo = ClassInfo(className = "LoxClass_$name"),
    superClassInfo = ClassInfo(className = "LoxCallable$arity"),
    accessFlagList = listOf(),
    interfaceList = listOf(),
    fieldList = listOf(),
    methodList = listOf(
      generateDefaultConstructor(className = "LoxCallable$arity"),
      generateConstructorCallMethod(className = name, arity = arity, methods = functionStuff),
      generateToStringMethod(output = "<class $name>"),
      generateArity(arity = arity),
    ),
    attributeList = listOf(),
  )
}

/**
 * Generates a `ClassFile` instance representing an instance class for the given name.
 *
 * @param name The name of the instance for which the class is being generated.
 * @return A `ClassFile` object that represents the generated instance class.
 */
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

private fun generateConstructorCallMethod(
  className: String,
  arity: Int,
  methods: List<FunctionStuff>
): MethodInfo {
  val loxInstanceCi = ClassInfo(className = "LoxInstance_$className")
  val code = mutableListOf(
    ShortConstantOperation(
      Opcode.OP_NEW,
      constant = loxInstanceCi,
      value = ObjectVti(classInfo = loxInstanceCi)
    ),
    SimpleOperation(Opcode.OP_DUP),
    ShortConstantOperation(
      Opcode.OP_INVOKE_SPECIAL,
      constant = generateConstructorMethodRef(className = "LoxInstance_$className")
    ),
  )

  for (method in methods) {
    code.add(SimpleOperation(Opcode.OP_DUP))
    code.addAll(
      elements = LoxFunction.generateInstantiationCode(
        functionName = "LoxMethod_${method.classItBelongsTo}_${method.name}",
        outerFunctionName = "",
        enclosedVariables = method.enclosedVariables,
        currentCodeOffset = code.size
      )
    )

    // here on the stack [object, function]

    // put the object into function
    code.add(SimpleOperation(Opcode.OP_DUP_2))
    code.add(SimpleOperation(Opcode.OP_SWAP))
    code.add(
      ShortConstantOperation(
        Opcode.OP_PUTFIELD, constant = JavaClass.generateFieldRef(
          className = "LoxBasicCallable",
          fieldName = "__this__",
        )
      )
    )

    // here on the stack should be [object, function] again

    code.add(
      ByteConstantOperation(
        Opcode.OP_LDC,
        constant = StringRefInfo(value = method.name),
        value = JavaString.VERIFICATION_TYPE
      )
    )
    code.add(ShortConstantOperation(Opcode.OP_INVOKE_VIRTUAL, constant = setInstanceFieldMethodRef()))
    code.add(SimpleOperation(Opcode.OP_POP)) // Remove LoxNil from the stack
  }

  if (hasInitializer(methods)) {
    // If there is an initializer, it should be called.
    code.add(SimpleOperation(Opcode.OP_DUP))
    code.add(SimpleOperation(Opcode.OP_NOP))
    code.add(
      ByteConstantOperation(
        Opcode.OP_LDC,
        constant = StringRefInfo("init"),
        value = JavaString.VERIFICATION_TYPE
      )
    )
    code.add(
      ByteConstantOperation(
        Opcode.OP_LDC,
        constant = StringRefInfo("No `init` error."),
        value = JavaString.VERIFICATION_TYPE
      )
    )
    code.add(ShortConstantOperation(Opcode.OP_INVOKE_VIRTUAL, constant = getInstanceFieldMethodRef()))

    code.add(
      ShortConstantOperation(
        Opcode.OP_CHECKCAST,
        constant = ClassInfo(className = "LoxCallable$arity")
      )
    )

    for (i in 1..arity) {
      code.add(OperationWithIndex(Opcode.OP_ALOAD, index = i.toByte(), value = loxObjectVti))
    }

    code.add(
      ShortConstantOperation(
        Opcode.OP_INVOKE_VIRTUAL,
        LoxFunction.generateCallMethodRef(arity)
      )
    )
    code.add(SimpleOperation(Opcode.OP_POP)) // Remove LoxNil from the stack
  }

  code.add(SimpleOperation(Opcode.OP_ARETURN))
  val codeAttribute = CodeAttribute(argsSize = 1, code = code)

  return MethodInfo(
    methodName = "__call__",
    accessFlagList = listOf(),
    attributeList = listOf(codeAttribute),
    signature = MethodSignature(
      arguments = generateMethodSignature(arity = arity),
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
    ShortConstantOperation(
      Opcode.OP_GETFIELD,
      constant = objectFieldMapReference(instanceClassName),
      value = JavaHashMap.VERIFICATION_TYPE
    ),
    SimpleOperation(Opcode.OP_ALOAD_1),
    ShortConstantOperation(Opcode.OP_INVOKE_VIRTUAL, constant = JavaHashMap.CONTAINS_KEY),
    ControlFlowOperation(Opcode.OP_IFEQ, jumpTo = 11),
    SimpleOperation(Opcode.OP_ALOAD_0),
    ShortConstantOperation(
      Opcode.OP_GETFIELD,
      constant = objectFieldMapReference(instanceClassName),
      value = JavaHashMap.VERIFICATION_TYPE
    ),
    SimpleOperation(Opcode.OP_ALOAD_1),
    ShortConstantOperation(Opcode.OP_INVOKE_VIRTUAL, constant = JavaHashMap.GET),
    ShortConstantOperation(Opcode.OP_CHECKCAST, constant = loxObjectClassInfo),
    SimpleOperation(Opcode.OP_ARETURN),
    ShortConstantOperation(
      Opcode.OP_NEW,
      constant = loxRuntimeErrorClassInfo,
      value = ObjectVti(loxRuntimeErrorClassInfo)
    ),
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
    ShortConstantOperation(
      Opcode.OP_GETFIELD,
      constant = objectFieldMapReference(instanceClassName),
      value = JavaHashMap.VERIFICATION_TYPE
    ),
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
    ShortConstantOperation(
      Opcode.OP_INVOKE_SPECIAL,
      constant = generateConstructorMethodRef(superClassName)
    ),
    SimpleOperation(Opcode.OP_ALOAD_0),
    ShortConstantOperation(
      Opcode.OP_NEW,
      constant = JavaHashMap.CLASS_INFO,
      value = JavaHashMap.VERIFICATION_TYPE
    ),
    SimpleOperation(Opcode.OP_DUP),
    ShortConstantOperation(
      Opcode.OP_INVOKE_SPECIAL,
      constant = JavaHashMap.CONSTRUCTOR
    ),
    ShortConstantOperation(
      Opcode.OP_PUTFIELD,
      constant = objectFieldMapReference(instanceClassName = thisClassName)
    ),
    SimpleOperation(Opcode.OP_RETURN)
  )

  return MethodInfo(
    methodName = "<init>",
    accessFlagList = listOf(),
    attributeList = listOf(CodeAttribute(code = code, argsSize = 1)),
    isStatic = false,
    signature = MethodSignature(arguments = listOf(), returnType = EmptyVti())
  )
}

private fun hasInitializer(methods: List<FunctionStuff>): Boolean {
  for (method in methods) {
    if (method.name == "init") {
      return true
    }
  }

  return false
}
