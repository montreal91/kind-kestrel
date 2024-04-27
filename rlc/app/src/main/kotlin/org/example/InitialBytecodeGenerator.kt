package org.example

import java.io.DataOutputStream
import java.io.FileOutputStream

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

    // #24
    dos.writeByte(10) // Methodref
    dos.writeShort(19) // java/io/PrintStream.println
    dos.writeShort(22) // (Ljava/lang/String;)V

    // #25
    dos.writeByte(9) // Fieldref
    dos.writeShort(18) // java/lang/System.out
    dos.writeShort(21) // Ljava/io/PrintStream

    // #26 Hello, World! string
    dos.writeByte(8)
    dos.writeShort(15)

    // ----------------------------------------------------------------------
    // Access flags
    dos.writeShort(1)  // Public

    // This class
    dos.writeShort(16) // Index of this class (HelloWorld)
    dos.writeShort(17) // Index of super class (java/lang/Object)

    // Interfaces
    dos.writeShort(0) // No interfaces

    // Fields
    dos.writeShort(0) // No fields

    // Methods
    dos.writeShort(1)  // Number of methods
    dos.writeShort(9)  // Method access flags (public static)
    dos.writeShort(9)  // Index of method name (main)
    dos.writeShort(10) // Index of method descriptor ([Ljava/lang/String;)V

    // Code attribute
    dos.writeShort(1) // Number of attributes (Code)
    dos.writeShort(1) // Index of attribute name (Code)
    dos.writeShort(1) // Max Stack
    dos.writeShort(1) // No local variables

    val codeLength = 12 // Length of the code
    dos.writeInt(codeLength) // Code length

    // Bytecode instructions
    dos.writeByte(18) // Opcode for ldc
    dos.writeShort(26) // Index of message string ("Hello, World!")
    dos.writeByte(182) // Opcode for invokevirtual
    dos.writeShort(24) // Index of method reference (println)
    dos.writeByte(177) // Opcode for return

    // Attributes
    dos.writeShort(0) // No attributes

    println("HelloWorld class file generated successfully: $fileName")
  }
}
