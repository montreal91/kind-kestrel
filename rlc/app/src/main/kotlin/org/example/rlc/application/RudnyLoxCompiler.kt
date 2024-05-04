package org.example.rlc.application

import org.example.rlc.frontend.toClassInfo
import org.example.rlc.frontend.toUtf8Value
import org.example.rlc.jvm.backend.CodeGenerator
import org.example.rlc.jvm.im.ClassAccessFlags
import org.example.rlc.jvm.im.ClassFile
import org.example.rlc.jvm.im.FieldRefInfo
import org.example.rlc.jvm.im.MethodAccessFlags
import org.example.rlc.jvm.im.MethodInfo
import org.example.rlc.jvm.im.MethodRefInfo
import org.example.rlc.jvm.im.NameAndTypeInfo
import org.example.rlc.jvm.im.Opcode
import org.example.rlc.jvm.im.Operation
import org.example.rlc.jvm.im.StringRefInfo

fun generateHelloWorldClass(): ClassFile {
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

  return ClassFile(
    thisClassInfo = "HelloWorld".toClassInfo(),
    superClassInfo = "java/lang/Object".toClassInfo(),
    accessFlagList = listOf(ClassAccessFlags.PUBLIC),
    interfaceList = listOf(),
    fieldList = listOf(),
    methodList = listOf(initMethodInfo, mainMethodInfo),
    attributeList = listOf()
  )
}

fun main() {
  val helloWorldClass = generateHelloWorldClass()
  CodeGenerator(rootDirectory = "").compileAll(listOf(helloWorldClass))
}
