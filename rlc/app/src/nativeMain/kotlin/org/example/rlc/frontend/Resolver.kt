package org.example.rlc.frontend

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import org.example.rlc.frontend.ast.Assignment
import org.example.rlc.frontend.ast.Ast
import org.example.rlc.frontend.ast.Binary
import org.example.rlc.frontend.ast.BlockStmt
import org.example.rlc.frontend.ast.CallExpr
import org.example.rlc.frontend.ast.Expr
import org.example.rlc.frontend.ast.ExprStmt
import org.example.rlc.frontend.ast.ForStmt
import org.example.rlc.frontend.ast.FunDeclStmt
import org.example.rlc.frontend.ast.Grouping
import org.example.rlc.frontend.ast.IfStmt
import org.example.rlc.frontend.ast.Literal
import org.example.rlc.frontend.ast.Logical
import org.example.rlc.frontend.ast.PrintStmt
import org.example.rlc.frontend.ast.ReturnStmt
import org.example.rlc.frontend.ast.Stmt
import org.example.rlc.frontend.ast.Unary
import org.example.rlc.frontend.ast.VarDeclStmt
import org.example.rlc.frontend.ast.Variable
import org.example.rlc.frontend.ast.WhileStmt
import org.example.rlc.frontend.scope.GlobalVariable
import org.example.rlc.frontend.scope.LocalVariable
import org.example.rlc.frontend.scope.VariableResolutionResult
import org.example.rlc.frontend.scope.VariableResolutionTable

@OptIn(ExperimentalUuidApi::class)
class Resolver {
  private val resolutionTable = VariableResolutionTable()
  private val frameStack = FrameStack()
  private val errors = mutableListOf<LoxCompileError>()

  val hasErrors = errors.isNotEmpty()

  fun resolve(program: Ast): VariableResolutionTable {
    println("__________________________________")
    println("Variable resolution stage started.\n")
    program.forEach(this::visitStmt)

    return resolutionTable
  }

  fun getErrors() = errors.toList()

  private fun visitStmt(stmt: Stmt) = when (stmt) {
    is BlockStmt -> visitBlockStmt(stmt)
    is ExprStmt -> visitExprStmt(stmt)
    is PrintStmt -> visitPrintStmt(stmt)
    is VarDeclStmt -> visitVarDeclStmt(stmt)
    is IfStmt -> visitIfStmt(stmt)
    is WhileStmt -> visitWhileStmt(stmt)
    is ForStmt -> visitForStmt(stmt)
    is FunDeclStmt -> visitFunDeclStmt(stmt)
    is ReturnStmt -> visitReturnStmt(stmt)
  }

  private fun visitExpr(expr: Expr) = when (expr) {
    is Literal -> visitLiteral()
    is Binary -> visitBinary(expr)
    is Grouping -> visitGrouping(expr)
    is Variable -> visitIdentifier(expr)
    is Logical -> visitLogical(expr)
    is Unary -> visitUnary(expr)
    is Assignment -> visitAssignment(expr)
    is CallExpr -> visitCallExpr(expr)
  }

  private fun visitBlockStmt(stmt: BlockStmt) {
    println("Resolver visiting a block statement.")
    frameStack.addNewFrame(Frame.Type.BLOCK)
    stmt.statements.forEach(this::visitStmt)
    frameStack.popFrame()
  }

  private fun visitExprStmt(stmt: ExprStmt) {
    visitExpr(stmt.expr)
  }

  private fun visitPrintStmt(stmt: PrintStmt) {
    visitExpr(stmt.expr)
  }

  private fun visitReturnStmt(stmt: ReturnStmt) {
    stmt.expr?.let(this::visitExpr)
  }

  private fun visitVarDeclStmt(stmt: VarDeclStmt) {
    checkVariable(stmt.token)
    resolveVariableDeclaration(stmt.uid, stmt.variable)

    stmt.initializer?.let(this::visitExpr)
  }

  private fun visitFunDeclStmt(stmt: FunDeclStmt) {
    checkVariable(stmt.identifier)
    resolveVariableDeclaration(stmt.uid, stmt.identifier.value)
    frameStack.addNewFrame(Frame.Type.FUNCTION)

    for (param in stmt.parameters) {
      checkVariable(param)
      frameStack.declareVariable(param.value)
    }

    visitBlockStmt(stmt.body)
    frameStack.popFrame()
  }

  private fun visitIfStmt(stmt: IfStmt) {
    visitExpr(stmt.expr)
    visitStmt(stmt.ifBranch)

    stmt.elseBranch?.let(this::visitStmt)
  }

  private fun visitWhileStmt(stmt: WhileStmt) {
    visitExpr(stmt.expr)
    visitStmt(stmt.body)
  }

  private fun visitForStmt(stmt: ForStmt) {
    stmt.initStmt?.let(this::visitStmt)
    stmt.conditionExpr?.let(this::visitExpr)
    stmt.updateExpr?.let(this::visitExpr)

    visitStmt(stmt.body)
  }

  private fun error(message: String, token: Token) {
    errors.add(LoxCompileError(message = message, token))
  }

  private fun visitLiteral() {}

  private fun visitBinary(expr: Binary) {
    println("Resolver visiting binary op: ${expr.operator}")
    visitExpr(expr.left)
    visitExpr(expr.right)
  }

  private fun visitGrouping(expr: Grouping) {
    visitExpr(expr.expression)
  }

  private fun visitIdentifier(expr: Variable) {
    println("Resolver visiting identifier: ${expr.variable}")
    resolutionTable.set(expr.uid, resolveVariable(expr.variable))
  }

  private fun visitLogical(expr: Logical) {
    visitExpr(expr.left)
    visitExpr(expr.right)
  }

  private fun visitUnary(expr: Unary) {
    visitExpr(expr.right)
  }

  private fun visitAssignment(expr: Assignment) {
    visitExpr(expr.left)
    visitExpr(expr.right)
  }

  private fun visitCallExpr(expr: CallExpr) {
    visitExpr(expr.callee)
    expr.args.forEach(this::visitExpr)
  }

  private fun resolveVariable(identifier: String): VariableResolutionResult =
    if (frameStack.existInAllFrames(identifier)) {
      println("Resolved to be local: $identifier")
      LocalVariable(frameStack.lookup(identifier))
    } else {
      GlobalVariable(name = identifier)
    }

  private fun checkVariable(variableToken: Token) {
    if (!frameStack.existInCurrentFrame(variableToken.value)) {
      return
    }

    error(message = "This identifier already exists.", variableToken)
  }

  private fun resolveVariableDeclaration(uid: Uuid, variable: String) {
    if (frameStack.isGlobal()) {
      resolutionTable.set(uid, GlobalVariable(variable))
    }
    else {
      frameStack.declareVariable(variable)
      resolutionTable.set(uid, LocalVariable(frameStack.lookup(variable)))
    }
  }
}
