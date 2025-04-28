@file:OptIn(ExperimentalUuidApi::class)

package org.example.rlc.frontend

import org.example.rlc.frontend.ast.Assign
import org.example.rlc.frontend.ast.Binary
import org.example.rlc.frontend.ast.BlockStmt
import org.example.rlc.frontend.ast.Call
import org.example.rlc.frontend.ast.ClassDeclStmt
import org.example.rlc.frontend.ast.Expr
import org.example.rlc.frontend.ast.ExprStmt
import org.example.rlc.frontend.ast.ForStmt
import org.example.rlc.frontend.ast.FormalParameter
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
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

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

@OptIn(ExperimentalUuidApi::class)
private fun defaultUidGen() = Uuid.random()

@OptIn(ExperimentalUuidApi::class)
class Parser(private val tokens: List<Token>, private val uidGen: () -> Uuid = ::defaultUidGen) {
  val hasErrors: Boolean get() = errors.isNotEmpty()

  private val statements = mutableListOf<MutableList<Stmt>>()
  private val errors = mutableListOf<LoxCompileError>()

  private var index = 0

  private val currentToken: Token get() = tokens[index]
  private val previous: Token get() = tokens[index - 1]
  private val isLastToken: Boolean get() = index == tokens.size - 1

  fun parse(): List<Stmt> {
    println("______________________")
    println("Parsing stage started.\n")
    statements.add(mutableListOf())
    program()
    println("Parsing stage ended.\n")
    println("____________________")
    return statements.last().toList()
  }

  fun getErrors() = errors.toList()

  private fun program() {
    while (!isLastToken) {
      declaration()
    }

    consume(expectedType = Token.Type.EOF, message = "Expect end of input.")
  }

  private fun declaration() = try {
    when (currentToken.type) {
      Token.Type.VAR -> varDecl()
      Token.Type.FUN -> funDecl()
      Token.Type.CLASS -> classDecl()
      else -> statement()
    }
  } catch (_: ParserException) {
    synchronize()
  }

  private fun varDecl() {
    consume(expectedType = Token.Type.VAR, message = "Expect 'var'")
    consume(expectedType = Token.Type.IDENTIFIER, message = "Expect identifier")
    val identifier = previous

    var expression: Expr? = null

    if (currentToken.type == Token.Type.EQUAL) {
      consume(Token.Type.EQUAL, message = "Expect '='")
      expression = expression()
    }

    val decl = VarDeclStmt(
      uid = uidGen(),
      variable = identifier.value,
      token = identifier,
      initializer = expression
    )

    consume(expectedType = Token.Type.SEMICOLON, message = "Expect ';' after value.")
    statements.last().add(decl)
  }

  private fun funDecl() {
    consume(expectedType = Token.Type.FUN, message = "Expect 'fun'.")
    function()
  }

  private fun classDecl() {
    consume(expectedType = Token.Type.CLASS, message = "Expect 'class'.")
    val methods = mutableListOf<FunDeclStmt>()

    consume(expectedType = Token.Type.IDENTIFIER, message = "Expect identifier after 'class'.")
    val identifier = previous
    val superclass = superclass()
    consume(expectedType = Token.Type.LEFT_BRACE, message = "Expect '{' after class name.")

    while (!isLastToken && currentToken.type == Token.Type.IDENTIFIER) {
      function()
      methods.add(statements.last().removeLast() as FunDeclStmt)
    }

    consume(expectedType = Token.Type.RIGHT_BRACE, message = "Expect '}' at the end of the class declaration.")
    statements.last().add(ClassDeclStmt(uid = uidGen(), identifier = identifier, superclass = superclass, methods = methods))
  }

  private fun superclass(): Token? {
    if (currentToken.type == Token.Type.LESS) {
      consume(expectedType = Token.Type.LESS, message = "Expect '<'")
      consume(expectedType = Token.Type.IDENTIFIER, message = "Expect identifier after '<'")
      return previous
    }

    return null
  }

  private fun statement() = when (currentToken.type) {
    Token.Type.PRINT -> printStatement()
    Token.Type.LEFT_BRACE -> block()
    Token.Type.IF -> ifStmt()
    Token.Type.WHILE -> whileStmt()
    Token.Type.FOR -> forStmt()
    Token.Type.RETURN -> returnStatement()
    else -> exprStatement()
  }

  private fun function() {
    consume(expectedType = Token.Type.IDENTIFIER, message = "Expect identifier after 'fun'.")
    val identifier = previous

    consume(expectedType = Token.Type.LEFT_PAREN, message = "Expect '(' after function identifier.")
    val formalArgs = parameters()
    consume(expectedType = Token.Type.RIGHT_PAREN, message = "Expect ')' after function parameters.")

    block()

    val body = statements.last().removeLast()
    val functionDecl = FunDeclStmt(uidGen(), identifier, formalArgs, body as BlockStmt)

    statements.last().add(functionDecl)
  }

  private fun parameters(): Array<FormalParameter> {
    val res = mutableListOf<FormalParameter>()
    if (currentToken.type == Token.Type.IDENTIFIER) {
      res.add(FormalParameter(uidGen(), currentToken))
      consume(Token.Type.IDENTIFIER, "Expect identifier.")
    }

    while (currentToken.type == Token.Type.COMMA && !isLastToken) {
      consume(Token.Type.COMMA, message = "Expect ','.")
      consume(Token.Type.IDENTIFIER, message = "Expect identifier after ','.")
      res.add(FormalParameter(uidGen(), previous))
    }

    return res.toTypedArray()
  }

  private fun block() {
    consume(Token.Type.LEFT_BRACE, message = "Expect '{'")
    statements.add(mutableListOf())

    while (!isLastToken && currentToken.type != Token.Type.RIGHT_BRACE) {
      declaration()
    }

    val block = BlockStmt(uid = uidGen(), statements = statements.removeLast())
    statements.last().add(block)

    consume(Token.Type.RIGHT_BRACE, message = "Expect '}' at the end of the block.")
  }

  private fun ifStmt() {
    consume(Token.Type.IF, message = "Expect if statement.")
    consume(Token.Type.LEFT_PAREN, message = "Expect '(' after if.")

    val expr = expression()

    consume(Token.Type.RIGHT_PAREN, message = "Expect ')' after expression.")

    statement()

    val ifBranch = statements.last().removeLast()
    val elseBranch = when(currentToken.type) {
      Token.Type.ELSE -> parseElse()
      else -> null
    }

    val ifStmt = IfStmt(uid = uidGen(), expr = expr, ifBranch = ifBranch, elseBranch = elseBranch)

    statements.last().add(ifStmt)
  }

  private fun whileStmt() {
    consume(Token.Type.WHILE, message = "Expect while statement.")
    consume(Token.Type.LEFT_PAREN, message = "Expect '(' after while.")

    val expr = expression()

    consume(Token.Type.RIGHT_PAREN, message = "Expect ')' after expression.")
    statement()

    val body = statements.last().removeLast()

    statements.last().add(WhileStmt(uidGen(), expr, body))
  }

  private fun forStmt() {
    consume(Token.Type.FOR, message = "Expect for statement.")
    consume(Token.Type.LEFT_PAREN, message = "Expect '(' after for.")

    val initStmt = when (currentToken.type) {
      Token.Type.VAR -> {
        varDecl()
        statements.last().removeLast()
      }
      Token.Type.SEMICOLON -> {
        consume(Token.Type.SEMICOLON, message = "Expected ';'.")
        null
      }
      else -> {
        exprStatement()
        statements.last().removeLast()
      }
    }

    val conditionExpr = when (currentToken.type == Token.Type.SEMICOLON) {
      true -> null
      false -> expression()
    }

    consume(Token.Type.SEMICOLON, message = "Expect ';' after init statement")

    val updateExpr = when (currentToken.type == Token.Type.RIGHT_PAREN) {
      true -> null
      false -> expression()
    }

    consume(Token.Type.RIGHT_PAREN, message = "Expect ')' after expression.")
    statement()

    val body = statements.last().removeLast()
    val forStmt = ForStmt(uidGen(), initStmt, conditionExpr, updateExpr, body)

    statements.last().add(forStmt)
  }

  private fun parseElse(): Stmt {
    consume(Token.Type.ELSE, message = "Expected 'else'.")
    statement()
    return statements.last().removeLast()
  }

  private fun printStatement() {
    consume(Token.Type.PRINT, message = "Expect print statement")
    val expr = expression()
    consume(Token.Type.SEMICOLON, message = "Expect ';' after value.")
    statements.last().add(PrintStmt(uidGen(), expr))
  }

  private fun exprStatement() {
    val expr = expression()
    consume(Token.Type.SEMICOLON, message = "Expect ';' after value.")
    statements.last().add(ExprStmt(uidGen(), expr))
  }

  private fun returnStatement() {
    consume(Token.Type.RETURN, message = "Expect return statement.")
    var expr: Expr? = null

    if (currentToken.type != Token.Type.SEMICOLON) {
      expr = expression()
    }

    consume(Token.Type.SEMICOLON, message = "Expect ';' after value.")
    statements.last().add(ReturnStmt(uid = uidGen(), expr = expr))
  }

  private fun expression(): Expr {
    return assignment()
  }

  private fun assignment(): Expr {
    val left = logicOr()
    println(
      "Prev: [${previous.type}, ${previous.value}] " +
          "Curr: [${currentToken.type}, ${currentToken.value}], " +
          "Left Type: [${left::class}]"
    )

    if (currentToken.type != Token.Type.EQUAL) {
      return left
    }

    val equalToken = currentToken
    consume(Token.Type.EQUAL, message = "Expect '=' on assignment expression")

    val right = assignment()

    return when (left) {
      is Get -> Set(uid = uidGen(), obj = left.obj, value = right, name = left.name)
      is Variable -> Assign(uid = uidGen(), left = left, right = right)

      else -> {
        println("Debug: Invalid assignment target. [$left]")
        throw error(message = "Invalid assignment target.", token = equalToken)
      }
    }
  }

  private fun logicOr(): Expr {
    var leftOperand = logicAnd()
    while (!isLastToken && currentToken.type == Token.Type.OR) {
      val token = currentToken
      consume(Token.Type.OR, message = "Expect 'or' after value.")
      val rightOperand: Expr = logicAnd()
      leftOperand = Logical(uidGen(), leftOperand, token, rightOperand)
    }

    return leftOperand
  }

  private fun logicAnd(): Expr {
    var leftOperand = equality()
    while (!isLastToken && currentToken.type == Token.Type.AND) {
      val token = currentToken
      consume(Token.Type.AND, message = "Expect 'and' after value.")
      val rightOperand = equality()
      leftOperand = Logical(uidGen(), leftOperand, token, rightOperand)
    }

    return leftOperand
  }

  private fun equality(): Expr {
    var leftOperand = comparison()
    while (!isLastToken && equalityTokens.contains(currentToken.type)) {
      val token = currentToken
      matchAny(equalityTokens.toList())
      val rightOperand = comparison()
      leftOperand = Binary(uidGen(), leftOperand, token, rightOperand)
    }

    return leftOperand
  }

  private fun comparison(): Expr {
    var leftOperand = term()
    while (!isLastToken && comparisonTokens.contains(currentToken.type)) {
      val token = currentToken
      matchAny(comparisonTokens.toList())
      val rightOperand = term()
      leftOperand = Binary(uidGen(), leftOperand, token, rightOperand)
    }

    return leftOperand
  }

  private fun term(): Expr {
    var leftOperand = factor()

    while (!isLastToken && currentToken.isPlusOrMinus()) {
      val token = currentToken
      matchAny(listOf(Token.Type.PLUS, Token.Type.MINUS))
      val rightOperand = factor()
      leftOperand = Binary(uidGen(), leftOperand, token, rightOperand)
    }

    return leftOperand
  }

  private fun factor(): Expr {
    var leftOperand = unary()

    while (!isLastToken && currentToken.isStarOrSlash()) {
      val token = currentToken
      matchAny(listOf(Token.Type.STAR, Token.Type.SLASH))
      val rightOperand = unary()
      leftOperand = Binary(uidGen(), leftOperand, token, rightOperand)
    }

    return leftOperand
  }

  private fun unary(): Expr {
    if (currentToken.isUnaryOperator()) {
      val token = currentToken
      matchAny(listOf(Token.Type.BANG, Token.Type.MINUS))
      return Unary(uid = uidGen(), operator = token, right = unary())
    }

    return call()
  }

  private fun call(): Expr {
    println("Parsing a call")
    var callee = primary()

    while (!isLastToken && (currentToken.type == Token.Type.LEFT_PAREN || currentToken.type == Token.Type.DOT)) {
      if (currentToken.type == Token.Type.LEFT_PAREN) {
        consume(Token.Type.LEFT_PAREN, message = "Expected '('.")
        val args = arguments()
        consume(Token.Type.RIGHT_PAREN, message = "Expected ')' after call arguments.")
        callee = Call(uidGen(), callee, args)
        continue
      }
      if (currentToken.type == Token.Type.DOT) {
        consume(Token.Type.DOT, message = "Expected '.'.")
        consume(Token.Type.IDENTIFIER, message = "Expected identifier after '.'.")
        val name = previous.value
        callee = Get(uidGen(), callee, name)
      }
    }

    return callee
  }

  private fun arguments(): List<Expr> {
    println("Parsing arguments.")
    val res = mutableListOf<Expr>()

    while (!isLastToken && currentToken.type != Token.Type.RIGHT_PAREN) {
      res.add(expression())

      if (currentToken.type == Token.Type.COMMA) {
        consume(Token.Type.COMMA, message = "Expected ','.")
      }
    }

    return res
  }

  private fun primary(): Expr {
    if (currentToken.isTerminal()) {
      val token = currentToken
      matchAny(terminals.toList())

      return when (token.type) {
        Token.Type.IDENTIFIER -> Variable(uidGen(), token.value)
        Token.Type.THIS -> This(uid = uidGen(), token = token)

        else -> Literal(uidGen(), token.value, tokenTypeToLiteralType(token.type))
      }
    }

    if (matchAny(listOf(Token.Type.LEFT_PAREN))) {
      val grouping = Grouping(uidGen(), expression())
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
    return ParserException(msg = "")
  }
}

private fun tokenTypeToLiteralType(tokenType: Token.Type) = when (tokenType) {
  Token.Type.NUMBER -> Literal.Type.NUMBER
  Token.Type.STRING -> Literal.Type.STRING
  Token.Type.TRUE -> Literal.Type.BOOLEAN
  Token.Type.FALSE -> Literal.Type.BOOLEAN
  Token.Type.NIL -> Literal.Type.NIL_TYPE
  else -> error("Token $tokenType does not represent a valid literal.")
}
