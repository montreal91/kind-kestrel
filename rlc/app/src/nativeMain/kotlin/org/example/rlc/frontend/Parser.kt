package org.example.rlc.frontend

import org.example.rlc.frontend.ast.Assignment
import org.example.rlc.frontend.ast.Binary
import org.example.rlc.frontend.ast.BlockStmt
import org.example.rlc.frontend.ast.Expr
import org.example.rlc.frontend.ast.ExprStmt
import org.example.rlc.frontend.ast.Grouping
import org.example.rlc.frontend.ast.Literal
import org.example.rlc.frontend.ast.Logical
import org.example.rlc.frontend.ast.PrintStmt
import org.example.rlc.frontend.ast.Stmt
import org.example.rlc.frontend.ast.Unary
import org.example.rlc.frontend.ast.VarDeclStmt
import org.example.rlc.frontend.ast.Variable
import org.example.rlc.frontend.ast.tokenTypeToLiteralType

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

private fun Token.isPlusOrMinus() = when (this.type) {
  Token.Type.PLUS -> true
  Token.Type.MINUS -> true
  else -> false
}


private fun Token.isStarOrSlash() = when (this.type) {
  Token.Type.STAR -> true
  Token.Type.SLASH -> true
  else -> false
}

private fun Token.isUnaryOperator() = when (this.type) {
  Token.Type.MINUS -> true
  Token.Type.BANG -> true
  else -> false
}

private val terminals = setOf(
  Token.Type.TRUE,
  Token.Type.FALSE,
  Token.Type.NIL,
  Token.Type.THIS,
  Token.Type.NUMBER,
  Token.Type.STRING,
  Token.Type.IDENTIFIER
)

private fun Token.isTerminal() = when {
  terminals.contains(this.type) -> true
  else -> false
}

private class ParserException(msg: String) : Exception(msg)

class Parser(private val tokens: List<Token>) {
  val hasErrors: Boolean get() = errors.isNotEmpty()

  private val statements = mutableListOf<MutableList<Stmt>>()
  private val errors = mutableListOf<LoxCompileError>()

  private var index = 0

  private val currentToken: Token get() = tokens[index]
  private val previous: Token get() = tokens[index - 1]
  private val isLastToken: Boolean get() = index == tokens.size - 1

  fun parse(): List<Stmt> {
    statements.add(mutableListOf())
    program()
    return statements.last().toList()
  }

  fun getErrors() = errors.toList()

  private fun program() {
    while (!isLastToken) {
      declaration()
    }

    consume(Token.Type.EOF, message = "Expect end of input.")
  }

  private fun declaration() {
    try {
      when (currentToken.type) {
        Token.Type.VAR -> varDecl()
        else -> statement()
      }
    } catch (e: ParserException) {
      synchronize()
    }
  }

  private fun varDecl() {
    consume(Token.Type.VAR, message = "Expect 'var'")
    consume(Token.Type.IDENTIFIER, message = "Expect identifier")
    val identifier = previous

    var expression: Expr? = null

    if (currentToken.type == Token.Type.EQUAL) {
      consume(Token.Type.EQUAL, message = "Expect '='")
      expression = expression()
    }

    val decl = VarDeclStmt(
      variable = identifier.value,
      token = identifier,
      initializer = expression
    )

    consume(Token.Type.SEMICOLON, message = "Expect ';' after value.")
    statements.last().add(decl)
  }

  private fun statement() = when (currentToken.type) {
    Token.Type.PRINT -> printStatement()
    Token.Type.LEFT_BRACE -> block()
    else -> exprStatement()
  }

  private fun block() {
    consume(Token.Type.LEFT_BRACE, message = "Expect '{'")
    statements.add(mutableListOf())

    while (!isLastToken && currentToken.type != Token.Type.RIGHT_BRACE) {
      declaration()
    }

    val block = BlockStmt(statements = statements.removeLast())
    statements.last().add(block)

    consume(Token.Type.RIGHT_BRACE, message = "Expect '}' at the end of the block.")
  }

  private fun printStatement() {
    consume(Token.Type.PRINT, message = "Expect print statement")
    val expr = expression()
    consume(Token.Type.SEMICOLON, message = "Expect ';' after value.")
    statements.last().add(PrintStmt(expr))
  }

  private fun exprStatement() {
    val expr = expression()
    consume(Token.Type.SEMICOLON, message = "Expect ';' after value.")
    statements.last().add(ExprStmt(expr))
  }

  private fun expression(): Expr {
    return assignment()
  }

  private fun assignment(): Expr {
    val left = logicOr()
    println(
      "Prev: [${previous.type}, ${previous.value}] " + "Curr: [${currentToken.type}, ${currentToken.value}], " + "Left Type: [${left::class}]"
    )

    if (currentToken.type != Token.Type.EQUAL) {
      return left
    }

    val equalToken = currentToken
    consume(Token.Type.EQUAL, message = "Expect '=' on assignment expression")

    val right = assignment()

    if (left is Variable) {
      return Assignment(left, right)
    }

    throw error(message = "Invalid assignment target.", token = equalToken)
  }

  private fun logicOr(): Expr {
    var leftOperand = logicAnd()
    while (!isLastToken && currentToken.type == Token.Type.OR) {
      val token = currentToken
      consume(Token.Type.OR, message = "Expect 'or' after value.")
      val rightOperand: Expr = logicAnd()
      leftOperand = Logical(leftOperand, token, rightOperand)
    }

    return leftOperand
  }

  private fun logicAnd(): Expr {
    var leftOperand = equality()
    while (!isLastToken && currentToken.type == Token.Type.AND) {
      val token = currentToken
      consume(Token.Type.AND, message = "Expect 'and' after value.")
      val rightOperand = equality()
      leftOperand = Logical(leftOperand, token, rightOperand)
    }

    return leftOperand
  }

  private fun equality(): Expr {
    var leftOperand = comparison()
    while (!isLastToken && equalityTokens.contains(currentToken.type)) {
      val token = currentToken
      matchAny(equalityTokens.toList())
      val rightOperand = comparison()
      leftOperand = Binary(leftOperand, token, rightOperand)
    }

    return leftOperand
  }

  private fun comparison(): Expr {
    var leftOperand = term()
    while (!isLastToken && comparisonTokens.contains(currentToken.type)) {
      val token = currentToken
      matchAny(comparisonTokens.toList())
      val rightOperand = term()
      leftOperand = Binary(leftOperand, token, rightOperand)
    }

    return leftOperand
  }

  private fun term(): Expr {
    var leftOperand = factor()

    while (!isLastToken && currentToken.isPlusOrMinus()) {
      val token = currentToken
      matchAny(listOf(Token.Type.PLUS, Token.Type.MINUS))
      val rightOperand = factor()
      leftOperand = Binary(leftOperand, token, rightOperand)
    }

    return leftOperand
  }

  private fun factor(): Expr {
    var leftOperand = unary()

    while (!isLastToken && currentToken.isStarOrSlash()) {
      val token = currentToken
      matchAny(listOf(Token.Type.STAR, Token.Type.SLASH))
      val rightOperand = unary()
      leftOperand = Binary(leftOperand, token, rightOperand)
    }

    return leftOperand
  }

  private fun unary(): Expr {
    if (currentToken.isUnaryOperator()) {
      val token = currentToken
      matchAny(listOf(Token.Type.BANG, Token.Type.MINUS))
      return Unary(operator = token, right = unary())
    }

    return call()
  }

  private fun call(): Expr {
    return primary()
  }

  private fun primary(): Expr {
    if (currentToken.isTerminal()) {
      val token = currentToken
      matchAny(terminals.toList())

      if (token.type == Token.Type.IDENTIFIER) {
        return Variable(token.value)
      }

      return Literal(token.value, tokenTypeToLiteralType(token.type))
    }

    if (matchAny(listOf(Token.Type.LEFT_PAREN))) {
      val grouping = Grouping(expression())
      matchAny(listOf(Token.Type.RIGHT_PAREN))
      return grouping
    }

    throw error(message = "Expect expression.", token = currentToken)
  }

  private fun consume(expectedType: Token.Type, message: String) = when (expectedType == currentToken.type) {
    true -> advance()
    else -> throw error(message = message, token = currentToken)
  }

  private fun advance() {
    if (index < tokens.size) {
      index++
    }
  }

  private fun matchAny(types: List<Token.Type>): Boolean {
    for (type in types) {
      if (currentToken.type == type) {
        advance()
        return true
      }
    }

    return false
  }

  private fun synchronize() {
    while (!isLastToken) {
      advance()

      if (previous.type == Token.Type.SEMICOLON) {
        return
      }
    }
  }

  private fun error(message: String, token: Token): ParserException {
    errors.add(LoxCompileError(message = message, token))
    return ParserException("")
  }
}
