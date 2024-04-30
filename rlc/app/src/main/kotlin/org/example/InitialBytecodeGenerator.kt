package org.example

import java.io.File
import kotlin.experimental.or


enum class Opcode(val value: Byte) {
  // Opcodes stored alphabetically by mnemonics
  OP_ALOAD_0(0x2a.toByte()),  // Load reference from local variable 0
  OP_GET_STATIC(0xb2.toByte()),  // Get static field from class
  OP_INVOKE_SPECIAL(0xb7.toByte()),  // Invoke instance method; direct invocation of instance initialization methods
  OP_INVOKE_VIRTUAL(0xb6.toByte()),  // Invoke instance method; dispatch based on class
  OP_LDC(0x12.toByte()),  // Push item from run-time constant pool
  OP_RETURN(0xb1.toByte()),  // Return void fromm method
}

enum class ClassAccessFlags(val value: Short) {
  PUBLIC(value = 0x0001),
}

enum class MethodAccessFlags(val value: Short) {
  PUBLIC(value = 0x0001),
  STATIC(value = 0x0008),
}

enum class ConstantType(val value: Byte) {
  // Values are stored alphabetically by type name
  CLASS(7.toByte()),
  FIELD_REF(9.toByte()),
  METHOD_REF(10.toByte()),
  NAME_AND_TYPE(12.toByte()),
  STRING(8.toByte()),
  UTF_8(1.toByte()),
}

fun Byte.toBytes() = listOf(this)

fun Short.toBytes(): List<Byte> {
  val res = mutableListOf<Byte>()
  res.add((this.toInt() shr 8).toByte())
  res.add(this.toByte())
  return res
}

fun Int.toBytes(): List<Byte> {
  val result = mutableListOf<Byte>()
  result.add((this shr 24).toByte())
  result.add((this shr 16).toByte())
  result.add((this shr 8).toByte())
  result.add(this.toByte())
  return result
}

class Operation(val opcode: Opcode, val operands: List<ConstantPoolInfo>)

class MethodInfo(
  val methodName: Utf8Value,
  val methodDescriptor: Utf8Value,
  val maxStack: Short,
  val maxLocals: Short,
  private val accessFlagList: List<MethodAccessFlags>,
  val code: List<Operation>
) {
  val accessFlags: Short
    get() {
      var res: Short = 0
      for (flag in accessFlagList) {
        res = res or flag.value
      }
      return res
    }
}

class InterfaceInfo
class FieldInfo
class AttributeInfo

class ClassFile(
  val thisClassInfo: ClassInfo,
  val superClassInfo: ClassInfo,
  val accessFlagList: List<ClassAccessFlags>,
  val interfaceList: List<InterfaceInfo>,
  val fieldList: List<FieldInfo>,
  val methodList: List<MethodInfo>,
  val attributeList: List<AttributeInfo>,
) {
  val accessFlags: Short
    get() {
      var res: Short = 0
      for (flag in accessFlagList) {
        res = res or flag.value
      }
      return res
    }

}

class ConstantPool {
  private val constants: MutableList<Constant> = mutableListOf()
  private val labelIndex: MutableMap<String, Int> = mutableMapOf()

  init {
    addConstantPoolInfo("Code".toUtf8Value())
  }

  private val size: Int
    get() = constants.size + 1

  operator fun get(label: String): Short = labelIndex[label]!!.toShort()

  fun addConstantPoolInfo(constantPoolInfo: ConstantPoolInfo) {
    if (labelIndex.containsKey(constantPoolInfo.label)) {
      return
    }

    when (constantPoolInfo) {
      is ClassInfo -> addClassInfo(constantPoolInfo)
      is ConstantValue -> addConstantValue(constantPoolInfo)
      is FieldRefInfo -> addFieldRefInfo(constantPoolInfo)
      is MethodRefInfo -> addMethodRefInfo(constantPoolInfo)
      is NameAndTypeInfo -> addNameAndTypeInfo(constantPoolInfo)
      is StringRefInfo -> addStringRefInfo(constantPoolInfo)
    }
  }

  fun toBytes(): List<Byte> {
    val res: MutableList<Byte> = mutableListOf()
    res.addAll(size.toShort().toBytes())

    for (constant in constants) {
      res.addAll(constant.toBytes())
    }

    return res
  }

  private fun addClassInfo(info: ClassInfo) {
    addConstantPoolInfo(info.className)
    addConstant(
      info.label,
      Constant(info.type, 1, getIndexAsByteArray(info.className.label))
    )
  }

  private fun addConstantValue(constantValue: ConstantValue) = when (constantValue) {
    is Utf8Value -> addConstant(
      constantValue.label,
      Constant(constantValue.type, constantValue.size, constantValue.value)
    )
    else -> {}
  }

  private fun addFieldRefInfo(info: FieldRefInfo) {
    addConstantPoolInfo(info.classInfo)
    addConstantPoolInfo(info.nameAndType)

    val constant = Constant(
      info.type,
      2,
      getTwoIndexesAsByteArray(info.classInfo.label, info.nameAndType.label)
    )
    addConstant(info.label, constant)
  }

  private fun addMethodRefInfo(info: MethodRefInfo) {
    addConstantPoolInfo(info.classInfo)
    addConstantPoolInfo(info.nameAndType)

    val constant = Constant(
      info.type,
      2,
      getTwoIndexesAsByteArray(info.classInfo.label, info.nameAndType.label)
    )
    addConstant(info.label, constant)
  }

  private fun addNameAndTypeInfo(info: NameAndTypeInfo) {
    addConstantPoolInfo(info.name)
    addConstantPoolInfo(info.descriptor)

    val value = getTwoIndexesAsByteArray(info.name.label, info.descriptor.label)
    addConstant(info.label, Constant(info.type, 1, value))
  }

  private fun addStringRefInfo(info: StringRefInfo) {
    addConstantPoolInfo(info.stringConstant)
    val value = getIndexAsByteArray(info.stringConstant.label)
    addConstant(info.label, Constant(info.type, 1, value))
  }

  private fun addConstant(label: String, constant: Constant) {
    constants.add(constant)
    labelIndex[label] = constants.size
  }

  // This function assumes that label exists in labelIndex
  private fun getIndexAsByteArray(label: String): ByteArray {
    return this[label].toBytes().toByteArray()
  }

  private fun getTwoIndexesAsByteArray(label1: String, label2: String): ByteArray {
    val byteArray1 = getIndexAsByteArray(label1)
    val byteArray2 = getIndexAsByteArray(label2)

    val result = ByteArray(size = byteArray1.size + byteArray2.size)

    byteArray1.copyInto(result)
    byteArray2.copyInto(result, destinationOffset = byteArray1.size)

    return result
  }
}

fun composeConstantPool(classFile: ClassFile): ConstantPool {
  val constantPool = ConstantPool()

  constantPool.addConstantPoolInfo(classFile.thisClassInfo)
  constantPool.addConstantPoolInfo(classFile.superClassInfo)

  for (method in classFile.methodList) {
    constantPool.addConstantPoolInfo(method.methodName)
    constantPool.addConstantPoolInfo(method.methodDescriptor)

    for (operation in method.code) {
      for (operand in operation.operands) {
        constantPool.addConstantPoolInfo(operand)
      }
    }
  }

  return constantPool
}

const val MAGIC_NUMBER = 0xCAFEBABE.toInt()
const val MINOR_VERSION = 0.toShort()
const val MAJOR_VERSION = 55.toShort()

fun compileOperation(operation: Operation, constantPool: ConstantPool): List<Byte> {
  val res = mutableListOf(operation.opcode.value)
  when (operation.opcode) {
    Opcode.OP_ALOAD_0 -> {}
    Opcode.OP_GET_STATIC -> {
      res.addAll(constantPool[operation.operands[0].label].toBytes())
    }
    Opcode.OP_INVOKE_SPECIAL -> {
      res.addAll(constantPool[operation.operands[0].label].toBytes())
    }
    Opcode.OP_INVOKE_VIRTUAL -> {
      res.addAll(constantPool[operation.operands[0].label].toBytes())
    }
    Opcode.OP_LDC -> {
      res.addAll(constantPool[operation.operands[0].label].toByte().toBytes())
    }
    Opcode.OP_RETURN -> {}
  }
  return res
}

fun compileCode(operations: List<Operation>, constantPool: ConstantPool): List<Byte> {
  val res = mutableListOf<Byte>()
  for (operation in operations) {
    res.addAll(compileOperation(operation, constantPool))
  }
  return res
}

fun compileMethodToByteCode(methodInfo: MethodInfo, constantPool: ConstantPool): List<Byte> {
  val res = mutableListOf<Byte>()
  res.addAll(methodInfo.accessFlags.toBytes())
  res.addAll(constantPool[methodInfo.methodName.label].toBytes())
  res.addAll(constantPool[methodInfo.methodDescriptor.label].toBytes())
  res.addAll(1.toShort().toBytes()) // For now, we'll compile just one code attribute

  val codeAttributeName = "Code"
  res.addAll(constantPool[codeAttributeName].toBytes())

  val codeItself = compileCode(methodInfo.code, constantPool)
  val attributeLength = (codeItself.size + 12).toBytes()
  res.addAll(attributeLength)
  res.addAll(methodInfo.maxStack.toBytes())
  res.addAll(methodInfo.maxLocals.toBytes())
  res.addAll(codeItself.size.toBytes())
  res.addAll(codeItself)
  res.addAll(0.toShort().toBytes()) // For now, exception table length is 0
  res.addAll(0.toShort().toBytes()) // For now, there are no additional attributes to a method
  return res
}

fun compileInterfaceToByteCode(interfaceInfo: InterfaceInfo, constantPool: ConstantPool): List<Byte> {
  // To be implemented
  return listOf()
}

fun compileFieldToByteCode(fieldRefInfo: FieldInfo, constantPool: ConstantPool): List<Byte> {
  // To be implemented
  return listOf()
}

fun compileAttributeToByteCode(attributeRefInfo: AttributeInfo, constantPool: ConstantPool): List<Byte> {
  // To be implemented
  return listOf()
}

fun compileClassToBytecode(classFile: ClassFile): ByteArray {
  val res = mutableListOf<Byte>()

  res.addAll(MAGIC_NUMBER.toBytes())
  res.addAll(MINOR_VERSION.toBytes())
  res.addAll(MAJOR_VERSION.toBytes())

  val constantPool = composeConstantPool(classFile = classFile)
  res.addAll(constantPool.toBytes())

  res.addAll(classFile.accessFlags.toBytes())
  res.addAll(constantPool[classFile.thisClassInfo.label].toBytes())
  res.addAll(constantPool[classFile.superClassInfo.label].toBytes())

  // Compile interfaces
  res.addAll(classFile.interfaceList.size.toShort().toBytes())
  for (anInterface in classFile.interfaceList) {
    res.addAll(compileInterfaceToByteCode(interfaceInfo = anInterface, constantPool = constantPool))
  }

  // Compile fields
  res.addAll(classFile.fieldList.size.toShort().toBytes())
  for (fieldRef in classFile.fieldList) {
    res.addAll(compileFieldToByteCode(fieldRefInfo = fieldRef, constantPool = constantPool))
  }

  // Compile methods
  res.addAll(classFile.methodList.size.toShort().toBytes())
  for (method in classFile.methodList) {
    res.addAll(compileMethodToByteCode(methodInfo = method, constantPool = constantPool))
  }

  // Compile attributes
  res.addAll(classFile.attributeList.size.toShort().toBytes())
  for (attribute in classFile.attributeList) {
    res.addAll(compileAttributeToByteCode(attribute, constantPool = constantPool))
  }

  return res.toByteArray()
}

sealed class ConstantPoolInfo {
  abstract val type: ConstantType
  abstract val label: String
  override fun toString() = "[$type] $label"
}

abstract class ConstantValue(
  override val type: ConstantType,
  override val label: String,
  open val value: ByteArray
) : ConstantPoolInfo()

class Utf8Value(
  label: String,
  value: ByteArray,
  val size: Short,
) : ConstantValue(type = ConstantType.UTF_8, label = label, value = value)

class NameAndTypeInfo(
  override val label: String,
  val name: Utf8Value,
  val descriptor: Utf8Value
) : ConstantPoolInfo() {
  override val type: ConstantType
    get() = ConstantType.NAME_AND_TYPE
}

class ClassInfo(
  override val label: String,
  val className: Utf8Value,
) : ConstantPoolInfo() {

  override val type: ConstantType
    get() = ConstantType.CLASS
}

class FieldRefInfo(
  override val label: String,
  val classInfo: ClassInfo,
  val nameAndType: NameAndTypeInfo
) : ConstantPoolInfo() {
  override val type: ConstantType
    get() = ConstantType.FIELD_REF
}

class MethodRefInfo(
  override val label: String,
  val classInfo: ClassInfo,
  val nameAndType: NameAndTypeInfo
) : ConstantPoolInfo() {
  override val type: ConstantType
    get() = ConstantType.METHOD_REF
}

class StringRefInfo(override val label: String, val stringConstant: Utf8Value) : ConstantPoolInfo() {
  override val type: ConstantType
    get() = ConstantType.STRING
}

// <init>:()V
val constructor = "<init>".toUtf8Value()
val voidDescriptor = "()V".toUtf8Value()
val objectClass = "java/lang/Object".toClassInfo()

val initializerNameAndType = NameAndTypeInfo(label = "<init>:V()", descriptor = voidDescriptor, name = constructor)

val objectInitializerMethodRef = MethodRefInfo(
  label = "Object.<init>()V",
  classInfo = objectClass,
  nameAndType = initializerNameAndType
)

val initMethodInfo = MethodInfo(
  methodName = constructor,
  methodDescriptor = voidDescriptor,
  accessFlagList = listOf(MethodAccessFlags.PUBLIC),
  maxStack = 1,
  maxLocals = 1,
  code = listOf(
    Operation(Opcode.OP_ALOAD_0, listOf()),
    Operation(Opcode.OP_INVOKE_SPECIAL, listOf(objectInitializerMethodRef)),
    Operation(Opcode.OP_RETURN, listOf()),
  )
)

val mainMethodName = "main".toUtf8Value()
val mainMethodDescriptor = "([Ljava/lang/String;)V".toUtf8Value()
val systemClassRef = "java/lang/System".toClassInfo()
val systemOutName = "out".toUtf8Value()
val systemOutType = "Ljava/io/PrintStream;".toUtf8Value()

val systemOutNameAndType = NameAndTypeInfo(
  label = "systemOutNameAndType",
  name = systemOutName,
  descriptor = systemOutType
)

val systemOutFieldRef = FieldRefInfo(
  label = "SystemOut",
  classInfo = systemClassRef, // System class info
  nameAndType = systemOutNameAndType, //
)

val greetingString = "Hello, World of JVM".toUtf8Value()
val greetingStringRef = StringRefInfo("greetingStringRef", greetingString)

val printString = "println".toUtf8Value()

val printDescriptor = "(Ljava/lang/String;)V".toUtf8Value()
val printStreamClassRef = "java/io/PrintStream".toClassInfo()

val printlnNameAndType = NameAndTypeInfo(
  label = "printlnNameAndType",
  name = printString,
  descriptor = printDescriptor
)

val printlnMethodRef = MethodRefInfo(
  label = "printlnMethodRef",
  classInfo = printStreamClassRef,
  nameAndType = printlnNameAndType
)

val mainMethodInfo = MethodInfo(
  methodName = mainMethodName,
  methodDescriptor = mainMethodDescriptor,
  accessFlagList = listOf(MethodAccessFlags.PUBLIC, MethodAccessFlags.STATIC),
  maxLocals = 1,
  maxStack = 2,
  code = listOf(
    Operation(Opcode.OP_GET_STATIC, listOf(systemOutFieldRef)),
    Operation(Opcode.OP_LDC, listOf(greetingStringRef)),
    Operation(Opcode.OP_INVOKE_VIRTUAL, listOf(printlnMethodRef)),
    Operation(Opcode.OP_RETURN, listOf()),
  )
)

fun String.toUtf8Value() = Utf8Value(
  label = this,
  size = this.length.toShort(),
  value = this.toByteArray(Charsets.UTF_8),
)

fun String.toClassInfo() = ClassInfo(label = this + "_classInfo", className = this.toUtf8Value())

val helloWorldClass = ClassFile(
  thisClassInfo = "HelloWorld".toClassInfo(),
  superClassInfo = "java/lang/Object".toClassInfo(),
  accessFlagList = listOf(ClassAccessFlags.PUBLIC),
  interfaceList = listOf(),
  fieldList = listOf(),
  methodList = listOf(initMethodInfo, mainMethodInfo),
  attributeList = listOf()
)

fun ByteArray.toShort() = ((this[0].toInt() shl 8) or (this[1].toInt() and 0xff)).toShort()

class Constant(private val type: ConstantType, private val size: Short, private val value: ByteArray) {
  fun toBytes(): List<Byte> {
    val res = mutableListOf(type.value)
    if (type == ConstantType.UTF_8) {
      res.addAll(size.toBytes())
    }
    res.addAll(value.toList())
    return res
  }

  override fun toString(): String = when (type) {
    ConstantType.UTF_8 -> "Constant UTF-8: [" + this.value.toString(Charsets.UTF_8) + "]"
    ConstantType.CLASS -> "Constant Class: [" + this.value.toShort() + "]"
    ConstantType.FIELD_REF -> "Constant Field: ["+ this.value.toShort() + " " + this.value.copyOfRange(2, 4).toShort() + "]"
    ConstantType.METHOD_REF -> "Constant MethodRef: ["+ this.value.toShort() + " " + this.value.copyOfRange(2, 4).toShort() + "]"
    ConstantType.NAME_AND_TYPE -> "Constant NameAndType: ["+ this.value.toShort() + " " + this.value.copyOfRange(2, 4).toShort() + "]"
    ConstantType.STRING -> "Constant String: [" + this.value.toShort() + "]"
  }
}

val bytecode = compileClassToBytecode(helloWorldClass)

fun ByteArray.writeToFile(filePathName: String) {
  val file = File("$filePathName.class")
  file.writeBytes(array = this)
}

fun main() {
  bytecode.writeToFile(filePathName = "HelloWorld")
}
