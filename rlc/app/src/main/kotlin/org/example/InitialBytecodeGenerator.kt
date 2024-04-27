package org.example

import java.io.DataOutputStream
import java.io.FileOutputStream

const val OP_RETURN: Byte = 0xb1.toByte()
const val OP_ALOAD_0: Byte = 0x2a.toByte()
const val OP_INVOKESPECIAL: Byte = 0xB7.toByte()
const val OP_GETSTATIC: Byte = 0xb2.toByte()
const val OP_LDC: Byte = 0x12.toByte()
const val OP_INVOKEVIRTUAL: Byte = 0xb6.toByte()


fun main() {
  // Define the class name and file name
  val className = "HelloWorld"
  val fileName = "$className.class"

  DataOutputStream(FileOutputStream(fileName)).use { dos ->
    // Magic number and version
    dos.writeInt(0xCAFEBABE.toInt())
    dos.writeShort(0) // Minor version
    dos.writeShort(52) // Major version (Java SE 8)

    // Constant pool
    val constantPoolCount = 27
    dos.writeShort(constantPoolCount) // Number of entries in the constant pool

    // #1
    dos.writeByte(1) // Type 1 - UTF-8
    dos.writeUTF("HelloWorld")

    // #2
    dos.writeByte(1) // Type 1 - UTF-8
    dos.writeUTF("java/lang/Object")

    // #3
    dos.writeByte(1) // Type 1 - UTF-8
    dos.writeUTF("<init>")

    // #4
    dos.writeByte(1) // Type 1 - UTF-8
    dos.writeUTF("()V")

    // #5
    dos.writeByte(1) //Type 1 - UTF-8
    dos.writeUTF("println")

    // #6
    dos.writeByte(1)
    dos.writeUTF("(Ljava/lang/String;)V")

    // #7
    dos.writeByte(1) // Type 1 - UTF-8
    dos.writeUTF("Code")

    // #8
    dos.writeByte(1) // Type 1 - UTF-8
    dos.writeUTF("LineNumberTable")

    // #9
    dos.writeByte(1) // Type 1 - UTF-8
    dos.writeUTF("main")

    // #10
    dos.writeByte(1) // Type 1 - UTF-8
    dos.writeUTF("([Ljava/lang/String;)V")

    // #11
    dos.writeByte(1) // Type 1 - UTF-8
    dos.writeUTF("java/lang/System")

    // #12
    dos.writeByte(1) // Type 1 - UTF-8
    dos.writeUTF("out")

    // #13
    dos.writeByte(1) // Type 1 - UTF-8
    dos.writeUTF("Ljava/io/PrintStream;")

    // #14
    dos.writeByte(1) // Type 1 - UTF-8
    dos.writeUTF("java/io/PrintStream")

    // #15
    dos.writeByte(1) // Type 1 - UTF-8
    dos.writeUTF("Hello, World!")

    // #16
    dos.writeByte(7) // Class
    dos.writeShort(1) // HelloWorld

    // #17
    dos.writeByte(7) // Class
    dos.writeShort(2) // java/lang/Object

    // #18
    dos.writeByte(7) // Class
    dos.writeShort(11) // java/lang/System

    // #19
    dos.writeByte(7)  // Class
    dos.writeShort(14) // java/io/PrintStream

    // #20
    dos.writeByte(12) // NameAndType
    dos.writeShort(3) // "<init>"
    dos.writeShort(4) // ()V

    // #21
    dos.writeByte(12) // NameAndType
    dos.writeShort(12) // out
    dos.writeShort(13) // Ljava/io/PrintStream

    // #22
    dos.writeByte(12) // Type 12 - NameAndType
    dos.writeShort(5) // println
    dos.writeShort(6) // (Ljava/lang/String;)V

    // #23
    dos.writeByte(10) // Methodref
    dos.writeShort(17) // java/lang/Object."<init>"
    dos.writeShort(20) // ()V
    val objectConstructorReference: Short = 23

    // #24
    dos.writeByte(10) // Methodref
    dos.writeShort(19) // java/io/PrintStream.println
    dos.writeShort(22) // (Ljava/lang/String;)V
    val printLnMethodRef: Short = 24

    // #25
    dos.writeByte(9) // Fieldref
    dos.writeShort(18) // java/lang/System.out
    dos.writeShort(21) // Ljava/io/PrintStream
    val systemOutField: Short = 25

    // #26 Hello, World! string
    dos.writeByte(8)
    dos.writeShort(15)
    val greetingStringRef: Short = 26

    // ----------------------------------------------------------------------
    // The main class access flags
    dos.writeShort(1) // Public

    // This class
    dos.writeShort(16) // Index of this class (HelloWorld)
    dos.writeShort(17) // Index of super class (java/lang/Object)

    // Interfaces
    dos.writeShort(0) // No interfaces

    // Fields
    dos.writeShort(0) // No fields

    // Methods
    dos.writeShort(2) // Number of methods

    // public HelloWorld();
    dos.writeShort(1) // Method access flags: ACC_PUBLIC
    dos.writeShort(3) // Index to constant pool entry for constructor name
    dos.writeShort(4) // Index to constant pool entry for constructor descriptor
    dos.writeShort(1) // attributes_count (just one code attribute)

    // Code attribute
    val constructorCode = byteArrayOf(
      OP_ALOAD_0,
      OP_INVOKESPECIAL,
      getHighByteFromShort(objectConstructorReference),
      objectConstructorReference.toByte(),
      OP_RETURN) // Return bytecode instruction (for void methods)
    dos.writeShort(7) // attribute_name_index: index to constant pool entry for "Code"
    dos.writeInt(constructorCode.size + 12) // attribute_length: size of the Code attribute
    dos.writeShort(1) // max_stack
    dos.writeShort(1) // max_locals
    dos.writeInt(constructorCode.size) // code_length: size of the bytecode instructions
    dos.write(constructorCode) // bytecode instructions

    // exception_table_length: 0 (no exception handlers)
    dos.writeShort(0)

    // attributes_count: 0 (no additional attributes)
    dos.writeShort(0)
    // ---------------------------------------------------------------------------------------------

    // public static main(String[] args);
    dos.writeShort(9) // Method access flags: ACC_PUBLIC, ACC_STATIC
    dos.writeShort(9) // Index to constant pool entry for main
    dos.writeShort(10) // Index to constant pool entry for main descriptor
    dos.writeShort(1) // attributes_count (just one code attribute)

    // Code attribute
    val mainCode = byteArrayOf(
      OP_GETSTATIC,
      getHighByteFromShort(systemOutField),
      systemOutField.toByte(),
      OP_LDC,
      greetingStringRef.toByte(),
      OP_INVOKEVIRTUAL,
      getHighByteFromShort(printLnMethodRef),
      printLnMethodRef.toByte(),
      OP_RETURN) // Return bytecode instruction (for void methods)
    dos.writeShort(7) // attribute_name_index: index to constant pool entry for "Code"
    dos.writeInt(mainCode.size + 12) // attribute_length: size of the Code attribute
    dos.writeShort(2) // max_stack
    dos.writeShort(1) // max_locals
    dos.writeInt(mainCode.size) // code_length: size of the bytecode instructions
    dos.write(mainCode) // bytecode instructions

    // exception_table_length: 0 (no exception handlers)
    dos.writeShort(0)

    // attributes_count: 0 (no additional attributes)
    dos.writeShort(0)
    // ---------------------------------------------------------------------------------------------

    // Class file attributes
    dos.writeShort(0)

    println("HelloWorld class file generated successfully: $fileName")
  }
}

fun getHighByteFromShort(value: Short) = (value.toInt() shr 8).toByte()
