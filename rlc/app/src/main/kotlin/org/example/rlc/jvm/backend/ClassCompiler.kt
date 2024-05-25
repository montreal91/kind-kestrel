package org.example.rlc.jvm.backend

import org.example.rlc.jvm.ir.AttributeInfo
import org.example.rlc.jvm.ir.ByteConstantOperation
import org.example.rlc.jvm.ir.ClassFile
import org.example.rlc.jvm.ir.ControlFlowOperation
import org.example.rlc.jvm.ir.FieldInfo
import org.example.rlc.jvm.ir.InterfaceInfo
import org.example.rlc.jvm.ir.MethodInfo
import org.example.rlc.jvm.ir.Operation
import org.example.rlc.jvm.ir.ShortConstantOperation
import org.example.rlc.jvm.ir.SimpleOperation

internal class ClassCompiler {
  private val constantPool : ConstantPool = ConstantPool()

  fun compileClass(classFile: ClassFile): ByteArray {
    val res = mutableListOf<Byte>()

    res.addAll(MAGIC_NUMBER.toBytes())
    res.addAll(MINOR_VERSION.toBytes())
    res.addAll(MAJOR_VERSION.toBytes())

    composeConstantPool(classFile = classFile)

    res.addAll(constantPool.toBytes())

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

    return res.toByteArray()
  }

  private fun composeConstantPool(classFile: ClassFile) {
    constantPool.addConstantPoolInfo(classFile.thisClassInfo)
    constantPool.addConstantPoolInfo(classFile.superClassInfo)

    for (method in classFile.methodList) {
      constantPool.addConstantPoolInfo(method.methodName)
      constantPool.addConstantPoolInfo(method.methodDescriptor)

      for (operation in method.code) {
        when (operation) {
          is ByteConstantOperation -> constantPool.addConstantPoolInfo(operation.constant)
          is ControlFlowOperation -> {}
          is ShortConstantOperation -> constantPool.addConstantPoolInfo(operation.constant)
          is SimpleOperation -> {}
        }
      }
    }
  }

  private fun compileAttributeToByteCode(attributeRefInfo: AttributeInfo): List<Byte> {
    // To be implemented
    return listOf()
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
    res.addAll(1.toShort().toBytes()) // For now, we'll compile just one code attribute

    val codeAttributeName = "Code"
    res.addAll(constantPool[codeAttributeName].toBytes())

    val codeItself = compileCode(methodInfo.code)
    val attributeLength = (codeItself.size + 12).toBytes()
    res.addAll(attributeLength)
    res.addAll(methodInfo.maxStack.toBytes())
    res.addAll(methodInfo.maxLocals.toBytes())
    res.addAll(codeItself.size.toBytes())
    res.addAll(codeItself)
    res.addAll(0.toShort().toBytes()) // For now, exception table length is 0
    res.addAll(0.toShort().toBytes()) // For now, there are no additional attributes to a method
    return res.toList()
  }

  private fun compileCode(operations: List<Operation>): List<Byte> {
    val res = mutableListOf<Byte>()
    val opIndex = MutableList<Short>(operations.size, init = {0})
    var ind: Short = 0

    for ((i, operation) in operations.withIndex()) {
      opIndex[i] = ind
      val bytes = compileOperation(operation)
      res.addAll(bytes)
      ind = (ind + bytes.size.toShort()).toShort()
    }

    for ((i, operation) in operations.withIndex()) {
      if (operation !is ControlFlowOperation) {
        continue
      }

      res.overwriteShort(
        start = opIndex[i] + 1,
        value = (opIndex[operation.jumpTo] - opIndex[i]).toShort()
      )
    }

    return res.toList()
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
