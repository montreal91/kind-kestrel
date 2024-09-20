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
import org.example.rlc.jvm.ir.ByteConstantOperation
import org.example.rlc.jvm.ir.ClassAccessFlags
import org.example.rlc.jvm.ir.ClassFile
import org.example.rlc.jvm.ir.CodeAttribute
import org.example.rlc.jvm.ir.ControlFlowOperation
import org.example.rlc.jvm.ir.DoubleValue
import org.example.rlc.jvm.ir.EmptyVti
import org.example.rlc.jvm.ir.MethodAccessFlags
import org.example.rlc.jvm.ir.MethodInfo
import org.example.rlc.jvm.ir.MethodRefInfo
import org.example.rlc.jvm.ir.MethodSignature
import org.example.rlc.jvm.ir.ObjectVti
import org.example.rlc.jvm.ir.Opcode
import org.example.rlc.jvm.ir.Operation
import org.example.rlc.jvm.ir.ShortConstantOperation
import org.example.rlc.jvm.ir.SimpleOperation
import org.example.rlc.jvm.ir.StringRefInfo
import org.example.rlc.jvm.ir.loxMainClassName


class AstToClassFileIrConverter {
  private val objectClass = javaLangObjectClassInfo
  private val classes = mutableListOf(
    loxClass(),
    loxObjectCf(),
    loxBooleanCf(),
    loxDoubleCf(),
    loxNilCf(),
    loxStringCf(),
    loxRuntimeError(),
  )
  private val currentCode = mutableListOf<Operation>()
  private val methodRefs = mutableMapOf<Token.Type, MethodRefInfo>()

  fun convert(roots: Ast): List<ClassFile> {
    roots.forEach { root -> visit(root) }
    finalizeClass()
    return classes.toList()
  }

  private fun finalizeClass() {
    val c = ClassFile(
      thisClassInfo = loxMainClassInfo,
      superClassInfo = objectClass,
      accessFlagList = listOf(ClassAccessFlags.PUBLIC),
      attributeList = listOf(),
      fieldList = listOf(),
      interfaceList = listOf(),
      methodList = listOf(
        constructor(),
        publicStaticVoidMain(),
        addMethod(),
        numberMagicMethod(methodName = "__sub__", returnType = "LoxDouble"),
        numberMagicMethod(methodName = "__mul__", returnType = "LoxDouble"),
        numberMagicMethod(methodName = "__div__", returnType = "LoxDouble"),
        unaryMinusMagicMethod(methodName = "__neg__"),
        notOperatorMagicMethod(),
        equalsMethod(),
        notEqualsMethod(),
        numberMagicMethod(methodName = "__gt__", returnType = "LoxBoolean"),
        numberMagicMethod(methodName = "__ge__", returnType = "LoxBoolean"),
        numberMagicMethod(methodName = "__lt__", returnType = "LoxBoolean"),
        numberMagicMethod(methodName = "__le__", returnType = "LoxBoolean"),
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
    Literal.Type.BOOLEAN -> compileBoolean(expr)
    Literal.Type.STRING -> compileString(expr)
    Literal.Type.NIL_TYPE -> compileNil()
  }

  private fun visitBinary(expr: Binary) {
    visitExpr(expr.left)
    visitExpr(expr.right)

    val methodRef = when (expr.operator.type) {
      Token.Type.PLUS -> getArithmeticMethodRef(Token.Type.PLUS)
      Token.Type.MINUS -> getArithmeticMethodRef(Token.Type.MINUS)
      Token.Type.STAR -> getArithmeticMethodRef(Token.Type.STAR)
      Token.Type.SLASH -> getArithmeticMethodRef(Token.Type.SLASH)
      Token.Type.EQUAL_EQUAL -> getArithmeticMethodRef(Token.Type.EQUAL_EQUAL)
      Token.Type.BANG_EQUAL -> getArithmeticMethodRef(Token.Type.BANG_EQUAL)
      Token.Type.GREATER -> getArithmeticMethodRef(Token.Type.GREATER)
      Token.Type.GREATER_EQUAL -> getArithmeticMethodRef(Token.Type.GREATER_EQUAL)
      Token.Type.LESS -> getArithmeticMethodRef(Token.Type.LESS)
      Token.Type.LESS_EQUAL -> getArithmeticMethodRef(Token.Type.LESS_EQUAL)
      else -> throw RuntimeException(
        "Unsupported binary operator [${expr.operator}]"
      )
    }

    currentCode.add(ShortConstantOperation(Opcode.OP_INVOKE_STATIC, methodRef))
  }

  private fun visitLogical(expr: Logical) {
    visitExpr(expr.left)
    when (expr.operator.type) {
      Token.Type.AND -> compileAnd(expr)
      Token.Type.OR -> compileOr()
      else -> throw IllegalStateException("Unsupported logical operator [${expr.operator}].")
    }
  }

  private fun compileAnd(expr: Logical) {
    currentCode.add(SimpleOperation(Opcode.OP_DUP))
    currentCode.add(ShortConstantOperation(Opcode.OP_INVOKE_VIRTUAL, loxObjectTruthyMri))
    currentCode.add(ShortConstantOperation(Opcode.OP_CHECKCAST, loxBooleanClassInfo))
    currentCode.add(ShortConstantOperation(Opcode.OP_GETFIELD, booleanValueFieldRefInfo))
    val insertBranchHere = currentCode.size
    // currentCode.add(ControlFlowOperation(Opcode.OP_IFEQ, n))
    // I guess that before we add operations from the second branch,
    // we need to pop duplicated boolean object from the stack.
    // Though, probably, we shouldn't duplicate the result of truthy method, but the resulting object itself.
    visitExpr(expr.right)
    val jumpToHere = currentCode.size + 1
    currentCode.add(insertBranchHere, ControlFlowOperation(Opcode.OP_IFEQ, jumpToHere))
  }

  private fun compileOr() {
    // Cuming during current cycle of feature implementation
  }

  private fun visitUnary(expr: Unary) {
    visitExpr(expr = expr.right)

    val methodRef = when (expr.operator.type) {
      Token.Type.MINUS -> getUnaryMethodRef(Token.Type.MINUS)
      Token.Type.BANG -> getUnaryMethodRef(Token.Type.BANG)
      else -> throw RuntimeException("Unexpected unary operator ${expr.operator}")
    }

    currentCode.add(ShortConstantOperation(Opcode.OP_INVOKE_STATIC, methodRef))
  }

  private fun visitGrouping(expr: Grouping) {
    visitExpr(expr.expression)
  }

  private fun compileBoolean(expr: Literal) {
    val valueOp = when (expr.value) {
      "true" -> Opcode.OP_ICONST_1
      "false" -> Opcode.OP_ICONST_0
      else -> throw RuntimeException("Unsupported boolean literal: [${expr.value}]")
    }

    val ops = listOf(
      ShortConstantOperation(Opcode.OP_NEW, loxBooleanClassInfo),
      SimpleOperation(Opcode.OP_DUP),
      SimpleOperation(valueOp),
      ShortConstantOperation(Opcode.OP_INVOKE_SPECIAL, loxBooleanConstructorInfo)
    )

    currentCode.addAll(ops)
  }

  private fun compileNumber(literal: Literal) {
    val ops = listOf(
      ShortConstantOperation(Opcode.OP_NEW, loxDoubleClassInfo),
      SimpleOperation(Opcode.OP_DUP),
      ShortConstantOperation(Opcode.OP_LDC2_W, DoubleValue(literal.value)),
      ShortConstantOperation(Opcode.OP_INVOKE_SPECIAL, loxDoubleConstructorInfo)
    )

    currentCode.addAll(ops)
  }

  private fun compileNil() {
    val ops = listOf(
      ShortConstantOperation(Opcode.OP_NEW, loxNilClassInfo),
      SimpleOperation(Opcode.OP_DUP),
      ShortConstantOperation(Opcode.OP_INVOKE_SPECIAL, loxNilConstructorInfo),
    )

    currentCode.addAll(ops)
  }

  private fun compileString(literal: Literal) {
    val ops = listOf(
      ShortConstantOperation(Opcode.OP_NEW, loxStringClassInfo),
      SimpleOperation(Opcode.OP_DUP),
      ByteConstantOperation(Opcode.OP_LDC, StringRefInfo(literal.value)),
      ShortConstantOperation(Opcode.OP_INVOKE_SPECIAL, loxStringConstructorInfo)
    )

    currentCode.addAll(ops)
  }

  private fun publicStaticVoidMain(): MethodInfo {
    currentCode.add(SimpleOperation(opcode = Opcode.OP_RETURN))
    return MethodInfo(
      methodName = "main",
      accessFlagList = listOf(MethodAccessFlags.PUBLIC, MethodAccessFlags.STATIC),
      attributeList = listOf(
        CodeAttribute(
          argsSize = 1,
          code = currentCode.toList(),
          exceptionTable = 0,
          attributes = listOf()
        )
      ),
      isStatic = true,
      signature = MethodSignature(
        listOf(ObjectVti(isArray = true, classInfo = javaLangStringClassInfo)),
        EmptyVti()
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
      methodName = constructorMethodName,
      accessFlagList = listOf(MethodAccessFlags.PUBLIC),
      attributeList = listOf(
        CodeAttribute(
          argsSize = 1,
          code = code,
          exceptionTable = 0,
          attributes = listOf()
        )
      ),
      isStatic = true,
      signature = MethodSignature(listOf(), EmptyVti())
    )
  }

  private fun getArithmeticMethodRef(operation: Token.Type): MethodRefInfo {
    return methodRefs.getOrPut(operation) {createBinaryNumericMethodRef(operation)}
  }

  private fun createBinaryNumericMethodRef(operation: Token.Type): MethodRefInfo {
    val nameAndType = binaryOperations[operation]
    val label = loxMainClassName + "." + nameAndType!!.label

    return MethodRefInfo(
      label = label,
      classInfo = loxMainClassInfo,
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
      classInfo = loxMainClassInfo,
      nameAndType = nameAndType,
      argsSize = 1,
      returnSize = 1
    )
  }
}
