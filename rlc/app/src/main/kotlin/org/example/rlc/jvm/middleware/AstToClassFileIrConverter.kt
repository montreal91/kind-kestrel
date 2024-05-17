package org.example.rlc.jvm.middleware

import org.example.rlc.frontend.ast.Ast
import org.example.rlc.jvm.ir.ClassFile
import org.example.rlc.jvm.ir.MethodAccessFlags
import org.example.rlc.jvm.ir.MethodInfo
import org.example.rlc.jvm.ir.NameAndTypeInfo
import org.example.rlc.jvm.ir.Opcode
import org.example.rlc.jvm.ir.Operation
import org.example.rlc.jvm.ir.toClassInfo
import org.example.rlc.jvm.ir.toUtf8Value

private data class Context(
  val className: String,
  val methodName: String
)

class AstToClassFileIrConverter(pathFile: String) {
  private val constructor = "<init>".toUtf8Value()
  private val noArgsVoidDescriptor = "()V".toUtf8Value()
  private val objectClass = "java/lang/Object.class".toClassInfo()
  private val loxMainClassName: String
  private val classes = mutableListOf<ClassFile>()

  init {
    val fileName = pathFile.substringAfterLast('/')
    loxMainClassName = fileName.substringBeforeLast('.') + "Lox"
  }

  fun convert(roots: Ast): List<ClassFile> {
    return classes
  }

  private fun constructor(): MethodInfo {
    val initializerNameAndType = NameAndTypeInfo(
      label = "<init>:V()", descriptor = noArgsVoidDescriptor, name = constructor
    )

    val code = listOf(
      Operation(Opcode.OP_ALOAD_0, listOf()),
      Operation(Opcode.OP_INVOKE_SPECIAL, listOf(initializerNameAndType)),
      Operation(Opcode.OP_RETURN, listOf())
    )

    return MethodInfo(
      methodName = constructor,
      methodDescriptor = noArgsVoidDescriptor,
      maxStack = 1,
      maxLocals = 1,
      accessFlagList = listOf(MethodAccessFlags.PUBLIC),
      code = code
    )
  }
}
