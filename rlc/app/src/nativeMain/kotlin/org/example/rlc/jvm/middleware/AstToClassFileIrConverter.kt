package org.example.rlc.jvm.middleware

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
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
import org.example.rlc.jvm.ir.ClassAccessFlags
import org.example.rlc.jvm.ir.ClassFile
import org.example.rlc.jvm.ir.CodeAttribute
import org.example.rlc.jvm.ir.MethodAccessFlags
import org.example.rlc.jvm.ir.MethodInfo
import org.example.rlc.jvm.ir.MethodRefInfo
import org.example.rlc.jvm.ir.Opcode
import org.example.rlc.jvm.ir.Operation
import org.example.rlc.jvm.ir.ShortConstantOperation
import org.example.rlc.jvm.ir.SimpleOperation
import org.example.rlc.jvm.ir.loxMainClassName
import org.example.rlc.jvm.ir.toClassInfo
import org.example.rlc.jvm.ir.toUtf8Value
import platform.posix.getenv


class AstToClassFileIrConverter {
  private val constructor = "<init>".toUtf8Value()
  private val noArgsVoidDescriptor = "()V".toUtf8Value()
  private val objectClass = "java/lang/Object".toClassInfo()
  private val classes = mutableListOf(
    loxClass(),
    loxObject(),
    loxDouble(),
    loxRuntimeError(),
  )
  private val currentCode = mutableListOf<Operation>()
  private val methodRefs = mutableMapOf<Token.Type, MethodRefInfo>()

  fun convert(roots: Ast): List<ClassFile> {
    roots.forEach { root -> visit(root) }
    finalizeClass()
    return classes.toList()
  }

  @OptIn(ExperimentalForeignApi::class)
  private val separatorChar: Char  get() {
    val separator = getenv(_VarName = "PATH_SEPARATOR")?.toKString()
    return separator?.firstOrNull() ?: '/'
  }

  private fun finalizeClass() {
    val c = ClassFile(
      thisClassInfo = loxMainClassName.toClassInfo(),
      superClassInfo = objectClass,
      accessFlagList = listOf(ClassAccessFlags.PUBLIC),
      attributeList = listOf(),
      fieldList = listOf(),
      interfaceList = listOf(),
      methodList = listOf(
        constructor(),
        publicStaticVoidMain(),
        addMethod(),
        numberMagicMethod(methodName = "__sub__"),
        numberMagicMethod(methodName = "__mul__"),
        numberMagicMethod(methodName = "__div__"),
        unaryMagicMethod(methodName = "__neg__"),
      )
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
    currentCode.add(ShortConstantOperation(Opcode.OP_GETSTATIC, systemOutField))
    visitExpr(printStmt.expr)
    currentCode.add(ShortConstantOperation(Opcode.OP_INVOKE_VIRTUAL, printMethodRef))
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
    visitExpr(expr.left)
    visitExpr(expr.right)

    val methodRef = when (expr.operator.type) {
      Token.Type.PLUS -> getArithmeticMethodRef(Token.Type.PLUS)
      Token.Type.MINUS -> getArithmeticMethodRef(Token.Type.MINUS)
      Token.Type.STAR -> getArithmeticMethodRef(Token.Type.STAR)
      Token.Type.SLASH -> getArithmeticMethodRef(Token.Type.SLASH)
      else -> throw RuntimeException(
        "Unsupported binary operator ${expr.operator}"
      )
    }

    currentCode.add(ShortConstantOperation(Opcode.OP_INVOKE_STATIC, methodRef))
  }

  private fun visitLogical(expr: Logical) {
    TODO("Not implemented yet.")
  }

  private fun visitUnary(expr: Unary) {
    visitExpr(expr = expr.right)

    val methodRef = when (expr.operator.type) {
      Token.Type.MINUS -> getUnaryMethodRef(Token.Type.MINUS)
      else -> throw RuntimeException("Unexpected unary operator ${expr.operator}")
    }

    currentCode.add(ShortConstantOperation(Opcode.OP_INVOKE_STATIC, methodRef))
  }

  private fun visitGrouping(expr: Grouping) {
    visitExpr(expr.expression)
  }

  private fun compileNumber(literal: Literal) {
    val ops = listOf(
      ShortConstantOperation(Opcode.OP_NEW, loxDoubleClassInfo),
      SimpleOperation(Opcode.OP_DUP),
      ShortConstantOperation(Opcode.OP_LDC2_W, literal.toConstant()),
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
          argsSize = 1,
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
          argsSize = 1,
          code = code,
          exceptionTable = 0,
          attributes = listOf()
        )
      )
    )
  }

  private fun getArithmeticMethodRef(operation: Token.Type): MethodRefInfo {
    return methodRefs.getOrPut(operation) {createArithmeticMethodRef(operation)}
  }

  private fun createArithmeticMethodRef(operation: Token.Type): MethodRefInfo {
    val nameAndType = binaryOperations[operation]
    val label = loxMainClassName + "." + nameAndType!!.label

    return MethodRefInfo(
      label = label,
      classInfo = loxMainClassName.toClassInfo(),
      nameAndType = nameAndType,
      argsSize = 2,
      returnSize = 1
    )
  }

  private fun getUnaryMethodRef(operation: Token.Type): MethodRefInfo {
    val nameAndType = unaryOperations[operation]
    val label = loxMainClassName + "." + nameAndType!!.label

    return MethodRefInfo(
      label = label,
      classInfo = loxMainClassName.toClassInfo(),
      nameAndType = nameAndType,
      argsSize = 1,
      returnSize = 1
    )
  }
}
