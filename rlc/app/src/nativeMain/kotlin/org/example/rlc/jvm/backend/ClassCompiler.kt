package org.example.rlc.jvm.backend

import org.example.rlc.jvm.ir.AttributeInfo
import org.example.rlc.jvm.ir.ByteConstantOperation
import org.example.rlc.jvm.ir.ClassFile
import org.example.rlc.jvm.ir.CodeAttribute
import org.example.rlc.jvm.ir.ControlFlowOperation
import org.example.rlc.jvm.ir.FieldInfo
import org.example.rlc.jvm.ir.InterfaceInfo
import org.example.rlc.jvm.ir.MethodInfo
import org.example.rlc.jvm.ir.Operation
import org.example.rlc.jvm.ir.SameFrame
import org.example.rlc.jvm.ir.ShortConstantOperation
import org.example.rlc.jvm.ir.SimpleOperation
import org.example.rlc.jvm.ir.StackMapFrame
import org.example.rlc.jvm.ir.StackMapTableAttribute

internal class ClassCompiler {
  private val constantPool : ConstantPool = ConstantPool()

  fun compileClass(classFile: ClassFile): List<Byte> {
    val res = mutableListOf<Byte>()

    res.addAll(MAGIC_NUMBER.toBytes())
    res.addAll(MINOR_VERSION.toBytes())
    res.addAll(MAJOR_VERSION.toBytes())

    composeConstantPool(classFile = classFile)

    val constantPoolIndex = res.size

    res.addAll(classFile.accessFlags.toBytes())
    res.addAll(constantPool[classFile.thisClassInfo.label].toBytes())
    res.addAll(constantPool[classFile.superClassInfo.label].toBytes())

    // Compile interfaces
    res.addAll(classFile.interfaceList.size.toShort().toBytes())
    for (anInterface in classFile.interfaceList) {
      res.addAll(compileInterfaceToByteCode(interfaceInfo = anInterface))
    }

    // Compile fields
    res.addAll(classFile.fieldList.size.toShort().toBytes())
    for (fieldRef in classFile.fieldList) {
      res.addAll(compileFieldToByteCode(fieldRefInfo = fieldRef))
    }

    // Compile methods
    res.addAll(classFile.methodList.size.toShort().toBytes())
    for (method in classFile.methodList) {
      res.addAll(compileMethodToByteCode(methodInfo = method))
    }

    // Compile attributes
    res.addAll(classFile.attributeList.size.toShort().toBytes())
    for (attribute in classFile.attributeList) {
      res.addAll(compileAttributeToByteCode(attribute))
    }

    res.addAll(constantPoolIndex, constantPool.toBytes())

    return res.toList()
  }

  private fun composeConstantPool(classFile: ClassFile) {
    constantPool.addConstantPoolInfo(classFile.thisClassInfo)
    constantPool.addConstantPoolInfo(classFile.superClassInfo)

    for (method in classFile.methodList) {
      constantPool.addConstantPoolInfo(method.methodName)
      constantPool.addConstantPoolInfo(method.methodDescriptor)

      for (attribute in method.attributes) {
        constantPool.addConstantPoolInfo(attribute.attributeName)
        when (attribute) {
          is CodeAttribute -> addCodeConstantsToConstantPool(attribute)
          is StackMapTableAttribute -> {}
        }
      }
    }
  }

  private fun addCodeConstantsToConstantPool(attribute: CodeAttribute) {
    constantPool.addConstantPoolInfo(attribute.attributeName)

    for (operation in attribute.code) {
      when (operation) {
        is ByteConstantOperation -> constantPool.addConstantPoolInfo(operation.constant)
        is ControlFlowOperation -> {}
        is ShortConstantOperation -> constantPool.addConstantPoolInfo(operation.constant)
        is SimpleOperation -> {}
      }
    }
  }

  private fun compileAttributeToByteCode(attribute: AttributeInfo): List<Byte> {
    constantPool.addConstantPoolInfo(attribute.attributeName)
    return when (attribute) {
      is CodeAttribute -> compileCodeAttribute(attribute)
      is StackMapTableAttribute -> compileStackMapTableAttribute(attribute)
    }
  }

  private fun compileFieldToByteCode(fieldRefInfo: FieldInfo): List<Byte> {
    val res = mutableListOf<Byte>()
    res.addAll(fieldRefInfo.accessFlags.toBytes())
    res.addAll(constantPool[fieldRefInfo.fieldName.label].toBytes())
    res.addAll(constantPool[fieldRefInfo.fieldDescriptor.label].toBytes())

    res.addAll(0.toShort().toBytes()) // No field attributes
    return res
  }

  private fun compileInterfaceToByteCode(interfaceInfo: InterfaceInfo): List<Byte> {
    // To be implemented
    return listOf()
  }

  private fun compileMethodToByteCode(methodInfo: MethodInfo): List<Byte> {
    val res = mutableListOf<Byte>()
    res.addAll(methodInfo.accessFlags.toBytes())
    res.addAll(constantPool[methodInfo.methodName.label].toBytes())
    res.addAll(constantPool[methodInfo.methodDescriptor.label].toBytes())

    res.addAll(methodInfo.attributes.size.toShort().toBytes())

    for (attribute in methodInfo.attributes) {
      res.addAll(compileAttributeToByteCode(attribute))
    }

    return res.toList()
  }

  private fun compileCodeAttribute(codeAttribute: CodeAttribute): List<Byte> {
    val res = mutableListOf<Byte>()

    res.addAll(constantPool[codeAttribute.attributeName.label].toBytes())

    val codeCompilationResult = compileCode(codeAttribute.code)

    if (!codeCompilationResult.stackMapTableAttribute.isEmpty()) {
      codeAttribute.addAttribute(codeCompilationResult.stackMapTableAttribute)
    }

    val codeItself = codeCompilationResult.bytes
    val attributeBytes = mutableListOf<Byte>()

    for (attribute in codeAttribute.allAttributes) {
      attributeBytes.addAll(compileAttributeToByteCode(attribute))
    }

    val attributeLength = (codeItself.size + attributeBytes.size + 12).toBytes()
    res.addAll(attributeLength)
    res.addAll(codeAttribute.maxStack.toBytes())
    res.addAll(codeAttribute.maxLocals.toBytes())
    res.addAll(codeItself.size.toBytes())
    res.addAll(codeItself)

    res.addAll(0.toShort().toBytes()) // For now, exception table length is 0

    res.addAll(codeAttribute.allAttributes.size.toShort().toBytes())
    res.addAll(attributeBytes)

    return res
  }

  private fun compileStackMapTableAttribute(stackMapTableAttribute: StackMapTableAttribute): List<Byte> {
    val res = mutableListOf<Byte>()
    res.addAll(constantPool[stackMapTableAttribute.attributeName.label].toBytes())

    val numberOfEntries = stackMapTableAttribute.frames.size.toShort()
    val entriesBytes = mutableListOf<Byte>()

    for (entry in stackMapTableAttribute.frames) {
      entriesBytes.addAll(entry.toBytes())
    }

    val attributeLength: Int = 2 + entriesBytes.size

    res.addAll(attributeLength.toBytes())
    res.addAll(numberOfEntries.toBytes())
    res.addAll(entriesBytes)

    return res
  }

  private fun compileCode(operations: List<Operation>): CodeCompilationResult {
    val res = mutableListOf<Byte>()
    val opIndex = MutableList<Short>(operations.size, init = {0})
    var ind: Short = 0

    for ((i, operation) in operations.withIndex()) {
      opIndex[i] = ind
      val bytes = compileOperation(operation)
      res.addAll(bytes)
      ind = (ind + bytes.size.toShort()).toShort()
    }

    val stackTable = mutableMapOf<Short, StackMapFrame>()
    for ((i, operation) in operations.withIndex()) {
      if (operation !is ControlFlowOperation) {
        continue
      }

      val offset = opIndex[operation.jumpTo]
      val jump = (offset - opIndex[i]).toShort()
      res.overwriteShort(
        start = opIndex[i] + 1,
        value = jump
      )

      // Here we have to add a stackTable entry
      stackTable[offset] = SameFrame(offset.toByte())
    }

    return CodeCompilationResult(
      bytes = res.toList(),
      stackMapTableAttribute = StackMapTableAttribute(stackTable.values.toList())
    )
  }

  private fun compileOperation(operation: Operation) = when (operation) {
    is SimpleOperation -> listOf(operation.opcode.value)
    is ByteConstantOperation -> compileSingleByteConstantOperation(operation)
    is ShortConstantOperation -> compileShortConstantOperation(operation)
    is ControlFlowOperation -> compileControlFlowOperation(operation)
  }

  private fun compileSingleByteConstantOperation(operation: ByteConstantOperation) = listOf(
    operation.opcode.value,
    constantPool[operation.constant.label].toByte()
  )

  private fun compileShortConstantOperation(operation: ShortConstantOperation) =
    operation.opcode.value.toBytes() + constantPool[operation.constant.label].toBytes()

  private fun compileControlFlowOperation(operation: ControlFlowOperation) =
    operation.opcode.value.toBytes() + 0.toShort().toBytes()
}
