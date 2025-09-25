package org.example.rlc.frontend

import co.touchlab.kermit.Logger
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import org.example.rlc.frontend.ast.Assign
import org.example.rlc.frontend.ast.Ast
import org.example.rlc.frontend.ast.Binary
import org.example.rlc.frontend.ast.BlockStmt
import org.example.rlc.frontend.ast.Call
import org.example.rlc.frontend.ast.ClassDeclStmt
import org.example.rlc.frontend.ast.Expr
import org.example.rlc.frontend.ast.ExprStmt
import org.example.rlc.frontend.ast.ForStmt
import org.example.rlc.frontend.ast.FunDeclStmt
import org.example.rlc.frontend.ast.Get
import org.example.rlc.frontend.ast.Grouping
import org.example.rlc.frontend.ast.IfStmt
import org.example.rlc.frontend.ast.Literal
import org.example.rlc.frontend.ast.Logical
import org.example.rlc.frontend.ast.PrintStmt
import org.example.rlc.frontend.ast.ReturnStmt
import org.example.rlc.frontend.ast.Set
import org.example.rlc.frontend.ast.Stmt
import org.example.rlc.frontend.ast.This
import org.example.rlc.frontend.ast.Unary
import org.example.rlc.frontend.ast.VarDeclStmt
import org.example.rlc.frontend.ast.Variable
import org.example.rlc.frontend.ast.WhileStmt
import org.example.rlc.frontend.scope.EnclosedVariable
import org.example.rlc.frontend.scope.GlobalVariable
import org.example.rlc.frontend.scope.LocalVariable
import org.example.rlc.frontend.scope.VariableResolutionResult
import org.example.rlc.frontend.scope.VariableResolutionTable

/**
 * A `Resolver` is responsible for the semantic analysis of an abstract syntax tree (AST)
 * in the context of variable resolution. It ensures that variables are properly declared
 * and resolves every variable to a specific scope, identifying whether it is global, local, or enclosed.
 *
 * This class runs the resolution process to analyze declarations, detect scoping issues,
 * and assign the appropriate variable resolution information for each identifier.
 */
@OptIn(ExperimentalUuidApi::class)
class Resolver {
  private val resolutionTable = VariableResolutionTable()
  private val frameStack = FrameStack()
  private val errors = mutableListOf<LoxCompileError>()

  private val log = Resolver::class.qualifiedName?.let { Logger.withTag(it) }

  val hasErrors = errors.isNotEmpty()

  fun resolve(program: Ast): VariableResolutionTable {
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
    is ClassDeclStmt -> visitClassDeclStmt(classDecl = stmt)
  }

  private fun visitExpr(expr: Expr) = when (expr) {
    is Literal -> visitLiteral()
    is Binary -> visitBinary(expr)
    is Grouping -> visitGrouping(expr)
    is Variable -> visitIdentifier(expr)
    is Logical -> visitLogical(expr)
    is Unary -> visitUnary(expr)
    is Assign -> visitAssignment(expr)
    is Call -> visitCallExpr(expr)
    is Get -> visitGetExpr(get = expr)
    is Set -> visitSetExpr(set = expr)
    is This -> visitThis(thisExpr = expr)
  }

  private fun visitBlockStmt(stmt: BlockStmt) {
    println("Resolver visiting a block statement.")
    frameStack.addNewFrame(type = Frame.Type.BLOCK, frameName = "Block")
    stmt.statements.forEach(action = this::visitStmt)
    frameStack.popFrame()
  }

  private fun visitExprStmt(stmt: ExprStmt) {
    visitExpr(expr = stmt.expr)
  }

  private fun visitPrintStmt(stmt: PrintStmt) {
    visitExpr(expr = stmt.expr)
  }

  private fun visitReturnStmt(stmt: ReturnStmt) {
    stmt.expr?.let(block = this::visitExpr)
  }

  private fun visitVarDeclStmt(stmt: VarDeclStmt) {
    checkVariable(stmt.token)
    resolveVariableDeclaration(stmt.uid, stmt.variable)

    stmt.initializer?.let(this::visitExpr)
  }

  private fun visitFunDeclStmt(stmt: FunDeclStmt) {
    checkVariable(stmt.identifier)
    resolveVariableDeclaration(stmt.uid, stmt.identifier.value)
    frameStack.addNewFrame(
      type = Frame.Type.FUNCTION,
      frameName = stmt.identifier.value
    )
    for (param in stmt.parameters) {
      checkVariable(param.identifier)
      resolveVariableDeclaration(uid = param.uid, variable = param.identifier.value)
    }

    visitBlockStmt(stmt.body)

    val frame = frameStack.popFrame()

    val enclosedVariables = frame.getVariables()
      .filter(::captureFilter)
      .toMap()

    for (ev in enclosedVariables) {
      val enclosedObject = when (ev.value.type) {
        VariableType.CAPTURED_LOCAL -> EnclosedLocal(ev.value.index)
        VariableType.CAPTURED_UPVALUE -> EnclosedUpvalue(variableName = ev.key)
        VariableType.LOCAL -> EnclosedLocal(-1)
      }

      stmt.enclosedVariables.add(EnclosedVariable(
        name = ev.key, depth = -1, enclosedObject = enclosedObject
      ))
    }
  }

  private fun visitClassDeclStmt(classDecl: ClassDeclStmt) {
    checkVariable(variableToken = classDecl.identifier)
    resolveVariableDeclaration(uid = classDecl.uid, variable = classDecl.identifier.value)

    classDecl.superclass?.let { it -> resolveVariable(it.value) }

    frameStack.addNewFrame(
      type = Frame.Type.CLASS,
      frameName = classDecl.identifier.value
    )

    classDecl.methods.forEach(action = this::visitFunDeclStmt)

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
    visitExpr(expr.left)
    visitExpr(expr.right)
  }

  private fun visitGrouping(expr: Grouping) {
    visitExpr(expr.expression)
  }

  private fun visitIdentifier(expr: Variable) {
    val resolvedVariable = resolveVariable(expr.variable)
    resolutionTable.set(expr.uid, resolvedVariable)
  }

  private fun visitLogical(expr: Logical) {
    visitExpr(expr.left)
    visitExpr(expr.right)
  }

  private fun visitUnary(expr: Unary) {
    visitExpr(expr.right)
  }

  private fun visitAssignment(expr: Assign) {
    visitExpr(expr.left)
    visitExpr(expr.right)
  }

  private fun visitCallExpr(expr: Call) {
    visitExpr(expr.callee)
    expr.args.forEach(this::visitExpr)
  }

  private fun visitGetExpr(get: Get) {
    visitExpr(expr = get.obj)
  }

  private fun visitSetExpr(set: Set) {
    visitExpr(expr = set.obj)
    visitExpr(expr = set.value)
  }

  private fun visitThis(thisExpr: This) {
    when (frameStack.isInsideMethod()) {
      true -> {}
      false -> error(message = "Can't use 'this' outside of a class", token = thisExpr.token)
    }
  }

  private fun resolveVariable(identifier: String): VariableResolutionResult {
    println("Resolving variable $identifier")
    println(frameStack)
    if (!frameStack.existInAllFrames(identifier)) {
      return GlobalVariable(name = identifier)
    }

    log?.d(messageString = "Resolved to be local or enclosed variable: $identifier")
    val lookup = frameStack.lookup(identifier)
    log?.d(lookup.toString())

    if (lookup.variableType != VariableType.LOCAL) {
      frameStack.markAsUpvalue(identifier)
      resolutionTable.updateAsUpvalue(lookup.declarationId)
    }

    return when (lookup.variableType) {
      VariableType.CAPTURED_LOCAL -> EnclosedVariable(
          name = identifier,
          enclosedObject = EnclosedLocal(lookup.index),
          depth = lookup.depth
      )
      VariableType.CAPTURED_UPVALUE -> EnclosedVariable(
        name = identifier,
        enclosedObject = EnclosedUpvalue(variableName = identifier),
        depth = lookup.depth,
      )
      VariableType.LOCAL -> LocalVariable(identifier, lookup.index, lookup.isUpvalue)
    }
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
    } else {
      frameStack.declareVariable(variable, uid)

      // Defines, in which a local variable array index
      // this variable should resolve to
      val lookup = frameStack.lookup(variable)
      resolutionTable.set(
        uid,
        LocalVariable(
          name = variable,
          variableArrayIndex = lookup.index,
          isUpValue = lookup.isUpvalue
        )
      )
    }
  }
}

private fun captureFilter(entry: Map.Entry<String, FrameVariable>): Boolean {
  return when (entry.value.type) {
    VariableType.CAPTURED_LOCAL -> true
    VariableType.CAPTURED_UPVALUE -> true
    VariableType.LOCAL -> false
  }
}
