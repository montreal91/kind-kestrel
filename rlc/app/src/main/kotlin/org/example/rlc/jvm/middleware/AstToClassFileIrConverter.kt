package org.example.rlc.jvm.middleware

import org.example.rlc.frontend.Token
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
import org.example.rlc.jvm.ir.ClassFile
import org.example.rlc.jvm.ir.MethodAccessFlags
import org.example.rlc.jvm.ir.MethodInfo
import org.example.rlc.jvm.ir.NameAndTypeInfo
import org.example.rlc.jvm.ir.Opcode
import org.example.rlc.jvm.ir.Operation
import org.example.rlc.jvm.ir.Utf8Value
import org.example.rlc.jvm.ir.toClassInfo
import org.example.rlc.jvm.ir.toDoubleValue
import org.example.rlc.jvm.ir.toStringRefInfo
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
  private val constantPool = ConstantPool()
  private val currentCode = mutableListOf<Operation>()

  init {
    val fileName = pathFile.substringAfterLast(delimiter = '/')
    loxMainClassName = fileName.substringBeforeLast(delimiter = '.') + "Lox"
  }

  fun convert(roots: Ast): List<ClassFile> {
    roots.forEach { root -> visit(root) }
    return classes
  }

  private fun visit(stmt: Stmt) = when (stmt) {
    is ExprStmt -> visitExprStmt(stmt)
    is PrintStmt -> visitPrintStmt(stmt)
  }

  private fun visitExprStmt(exprStmt: ExprStmt): Unit = visitExpr(exprStmt.expr)

  private fun visitPrintStmt(printStmt: PrintStmt) {
    visitExpr(printStmt.expr)
  }

  private fun visitExpr(expr: Expr): Unit = when (expr) {
    is Binary -> visitBinary(expr)
    is Grouping -> visitGrouping(expr)
    is Literal -> visitLiteral(expr)
    is Logical -> visitLogical(expr)
    is Unary -> visitUnary(expr)
  }

  private fun visitLiteral(expr: Literal) = when (expr.type) {
    Literal.Type.NUMBER -> compileNumber(expr)
    Literal.Type.BOOLEAN -> {
      TODO()
    }

    Literal.Type.STRING -> compileString(expr)
    Literal.Type.NIL_TYPE -> {
      TODO()
    }
  }

  private fun visitBinary(expr: Binary) {
    visitExpr(expr.left)
    visitExpr(expr.right)
    when (expr.operator.type) {
      Token.Type.PLUS ->
    }
  }
  private fun visitLogical(expr: Logical) {}
  private fun visitUnary(expr: Unary) {}

  private fun visitGrouping(expr: Grouping) = visitExpr(expr.expression)

  private fun compileNumber(literal: Literal) {
    val operation = Operation(
      opcode = Opcode.OP_LDC,
      operands = listOf(literal.value.toDouble().toDoubleValue()),
    )
    currentCode.add(operation)
  }

  private fun compileString(literal: Literal) {
    val operation = Operation(
      operands = listOf(literal.value.toStringRefInfo()),
      opcode = Opcode.OP_LDC,
    )
    currentCode.add(operation)
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
