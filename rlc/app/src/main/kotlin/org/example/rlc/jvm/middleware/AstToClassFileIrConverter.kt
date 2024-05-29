package org.example.rlc.jvm.middleware

import java.io.File
import org.example.rlc.frontend.ast.Ast
import org.example.rlc.frontend.ast.Binary
import org.example.rlc.frontend.ast.Expr
import org.example.rlc.frontend.ast.ExprStmt
import org.example.rlc.frontend.ast.Grouping
import org.example.rlc.frontend.ast.Literal
import org.example.rlc.frontend.ast.Logical
import org.example.rlc.frontend.ast.PrintStmt
import org.example.rlc.frontend.ast.Stmt
import org.example.rlc.frontend.ast.Unary
import org.example.rlc.jvm.backend.ConstantPool
import org.example.rlc.jvm.ir.ClassAccessFlags
import org.example.rlc.jvm.ir.ClassFile
import org.example.rlc.jvm.ir.CodeAttribute
import org.example.rlc.jvm.ir.MethodAccessFlags
import org.example.rlc.jvm.ir.MethodInfo
import org.example.rlc.jvm.ir.Opcode
import org.example.rlc.jvm.ir.Operation
import org.example.rlc.jvm.ir.ShortConstantOperation
import org.example.rlc.jvm.ir.SimpleOperation
import org.example.rlc.jvm.ir.toClassInfo
import org.example.rlc.jvm.ir.toUtf8Value

private data class Context(
  val className: String,
  val methodName: String
)

enum class RuntimeType {
  DOUBLE, STRING, BOOLEAN, NIL, REFERENCE
}


class AstToClassFileIrConverter(pathFile: String) {
  private val constructor = "<init>".toUtf8Value()
  private val noArgsVoidDescriptor = "()V".toUtf8Value()
  private val objectClass = "java/lang/Object".toClassInfo()
  private val loxMainClassName: String
  private val classes = mutableListOf(
    loxClass(),
    loxObject(),
    loxDouble(),
    loxRuntimeError(),
  )
  private val constantPool = ConstantPool()
  private val currentCode = mutableListOf<Operation>()

  init {
    val fileName = pathFile.substringAfterLast(delimiter = File.separatorChar)
    loxMainClassName = fileName.substringBeforeLast(delimiter = '.') + "Lox"
    println("PATH Separator: [${File.separatorChar}]")
    println("PATH:           [$pathFile]")
    println("FileName:       [$fileName]")
    println("LoxMainClass:   [$loxMainClassName]")
  }

  fun convert(roots: Ast): List<ClassFile> {
    roots.forEach { root -> visit(root) }
    finalizeClass()
    return classes.toList()
  }

  private fun finalizeClass() {
    val c = ClassFile(
      thisClassInfo = loxMainClassName.toClassInfo(),
      superClassInfo = objectClass,
      accessFlagList = listOf(ClassAccessFlags.PUBLIC),
      attributeList = listOf(),
      fieldList = listOf(),
      interfaceList = listOf(),
      methodList = listOf(constructor(), publicStaticVoidMain(), addMethod())
    )
    classes.add(c)
  }

  private fun visit(stmt: Stmt) = when (stmt) {
    is ExprStmt -> visitExprStmt(stmt)
    is PrintStmt -> visitPrintStmt(stmt)
  }

  private fun visitExprStmt(exprStmt: ExprStmt) {
    visitExpr(exprStmt.expr)
  }

  private fun visitPrintStmt(printStmt: PrintStmt) {
    visitExpr(printStmt.expr)
  }

  private fun visitExpr(expr: Expr) = when (expr) {
    is Binary -> visitBinary(expr)
    is Grouping -> visitGrouping(expr)
    is Literal -> visitLiteral(expr)
    is Logical -> visitLogical(expr)
    is Unary -> visitUnary(expr)
  }

  private fun visitLiteral(expr: Literal) = when (expr.type) {
    Literal.Type.NUMBER -> compileNumber(expr)
    Literal.Type.BOOLEAN -> {}  // TODO()
    Literal.Type.STRING -> compileString(expr)
    Literal.Type.NIL_TYPE -> {
      TODO()
    }
  }

  private fun visitBinary(expr: Binary) {
    TODO("Not implemented yet.")
  }

  private fun visitLogical(expr: Logical) {
    TODO("Not implemented yet.")
  }

  private fun visitUnary(expr: Unary) {
    TODO("Not implemented yet.")
  }

  private fun visitGrouping(expr: Grouping) {
    visitExpr(expr.expression)
  }

  private fun compileNumber(literal: Literal) {
    val ops = listOf(
      ShortConstantOperation(Opcode.OP_NEW, loxDoubleClassInfo),
      SimpleOperation(Opcode.OP_DUP),
      ShortConstantOperation(Opcode.OP_LDC_2W, literal.toConstant()),
      ShortConstantOperation(Opcode.OP_INVOKE_SPECIAL, loxDoubleConstructorInfo)
    )

    currentCode.addAll(ops)
  }

  private fun compileString(literal: Literal) {
    TODO("Not implemented yet.")
  }

  private fun publicStaticVoidMain(): MethodInfo {
    currentCode.add(SimpleOperation(opcode = Opcode.OP_RETURN))
    return MethodInfo(
      methodName = "main".toUtf8Value(),
      methodDescriptor = "([Ljava/lang/String;)V".toUtf8Value(),
      accessFlagList = listOf(MethodAccessFlags.PUBLIC, MethodAccessFlags.STATIC),
      attributes = listOf(
        CodeAttribute(
          maxStack = 10,
          maxLocals = 2,
          code = currentCode.toList(),
          exceptionTable = 0,
          attributes = listOf()
        )
      )
    )
  }

  private fun constructor(): MethodInfo {
    val code = listOf(
      SimpleOperation(Opcode.OP_ALOAD_0),
      ShortConstantOperation(Opcode.OP_INVOKE_SPECIAL, objectConstructor),
      SimpleOperation(Opcode.OP_RETURN)
    )

    return MethodInfo(
      methodName = constructor,
      methodDescriptor = noArgsVoidDescriptor,
      accessFlagList = listOf(MethodAccessFlags.PUBLIC),
      attributes = listOf(
        CodeAttribute(
          maxStack = 1,
          maxLocals = 1,
          code = code,
          exceptionTable = 0,
          attributes = listOf()
        )
      )
    )
  }
}
