package org.example.rlc.frontend

import org.example.rlc.frontend.ast.Expr
import org.example.rlc.frontend.ast.ExprStmt
import org.example.rlc.frontend.ast.Literal
import org.example.rlc.frontend.ast.Logical
import org.example.rlc.frontend.ast.PrintStmt
import org.example.rlc.frontend.ast.Stmt

private val equalityTokens = setOf(
  Token.Type.EQUAL_EQUAL,
  Token.Type.BANG_EQUAL,
)

private val comparisonTokens = setOf(
  Token.Type.GREATER,
  Token.Type.GREATER_EQUAL,
  Token.Type.LESS,
  Token.Type.LESS_EQUAL,
)

class Parser(private val tokens: List<Token>) {
  val hasErrors: Boolean get() = errors.isNotEmpty()

  private val statements = mutableListOf<Stmt>()
  private val errors = mutableListOf<LoxCompileError>()

  private var index = 0

  private val currentToken: Token get() = tokens[index]
  private val previous: Token get() = tokens[index - 1]
  private val isLastToken: Boolean get() = index == tokens.size - 1

  fun parse(): List<Stmt> {
    program()
    return statements
  }

  private fun program() {
    while (!isLastToken) {
      declaration()
    }

    consume(Token.Type.EOF)
  }

  private fun declaration() {
    statement()
  }

  private fun statement() = when(currentToken.type) {
    Token.Type.PRINT -> printStatement()
    else -> exprStatement()
  }

  private fun printStatement() {
    consume(Token.Type.PRINT)
    val expr = expression()
    consume(Token.Type.SEMICOLON)
    statements.add(PrintStmt(expr))

  }

  private fun exprStatement() {
    val expr = expression()
    consume(Token.Type.SEMICOLON)
    statements.add(ExprStmt(expr));
  }

  private fun expression(): Expr {
    return assignment()
  }

  private fun assignment(): Expr {
    return logicOr()
  }

  private fun logicOr(): Expr {
    var leftOperand: Expr = logicAnd()
    while (!isLastToken && currentToken.type == Token.Type.OR) {
      val token = currentToken
      consume(Token.Type.OR)
      val rightOperand: Expr = logicAnd()
      leftOperand = Logical(leftOperand, token, rightOperand)
    }

    return leftOperand
  }

  private fun logicAnd(): Expr {
    var leftOperand: Expr = equality()
    while (!isLastToken && currentToken.type == Token.Type.AND) {
      val token = currentToken
      consume(Token.Type.AND)
      val rightOperand: Expr = equality()
      leftOperand = Logical(leftOperand, token, rightOperand)
    }

    return leftOperand
  }

  private fun equality(): Expr {
    var leftOperand: Expr = comparison()
    while (!isLastToken && equalityTokens.contains(currentToken.type)) {
      val token = currentToken
      match(equalityTokens.toList())
      val rightOperand: Expr = comparison()
      leftOperand = Logical(leftOperand, token, rightOperand)
    }
    return leftOperand
  }

  private fun comparison(): Expr {
    return term()
  }

  private fun term(): Expr {
    return factor()
  }

  private fun factor(): Expr {
    return unary()
  }

  private fun unary(): Expr {
    return call()
  }

  private fun call(): Expr {
    return primary()
  }

  private fun primary(): Expr {
    return Literal(currentToken.value, Literal.Type.STRING)
  }

  private fun consume(expectedType: Token.Type) = when (expectedType == currentToken.type) {
    true -> advance()
    else -> error("Unexpected token. Expected type: $expectedType", currentToken)
  }


  private fun advance() {
    if (index < tokens.size) {
      index++
    }
  }

  private fun match(types: List<Token.Type>): Boolean {
    for (type in types) {
      if (currentToken.type == type) {
        advance()
        return true
      }
    }

    error("Unexpected token.", currentToken)
    return false
  }

  private fun error(message: String, token: Token) {
    errors.add(LoxCompileError(message = message, token.lineNumber))
  }
}
