package org.example.rlc.jvm.backend

import org.example.rlc.jvm.ir.AttributeInfo
import org.example.rlc.jvm.ir.ClassFile
import org.example.rlc.jvm.ir.FieldInfo
import org.example.rlc.jvm.ir.InterfaceInfo
import org.example.rlc.jvm.ir.MethodInfo
import org.example.rlc.jvm.ir.Opcode
import org.example.rlc.jvm.ir.Operation

class CodeGenerator(private val buildOutputDir: String) {
  private val constantPool = ConstantPool()

  fun compileAll(classes: List<ClassFile>) {
    classes.forEach(this::classToFile)
  }

  private fun classToFile(classFile: ClassFile) {
    val bytecode = compileClassToBytecode(classFile)

    val filepath = when (buildOutputDir == "") {
      true -> classFile.thisClassInfo.className.label
      false -> buildOutputDir + "/" + classFile.thisClassInfo.className.label
    }

    bytecode.writeToFile(filepath)
  }

  private fun compileClassToBytecode(classFile: ClassFile): ByteArray {
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
        for (operand in operation.operands) {
          constantPool.addConstantPoolInfo(operand)
        }
      }
    }
  }

  private fun compileAttributeToByteCode(attributeRefInfo: AttributeInfo): List<Byte> {
    // To be implemented
    return listOf()
  }

  private fun compileFieldToByteCode(fieldRefInfo: FieldInfo): List<Byte> {
    // To be implemented
    return listOf()
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
    return res
  }

  private fun compileCode(operations: List<Operation>): List<Byte> {
    val res = mutableListOf<Byte>()
    for (operation in operations) {
      res.addAll(compileOperation(operation))
    }
    return res
  }

  private fun compileOperation(operation: Operation): List<Byte> {
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
}
