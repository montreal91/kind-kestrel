package com.nay.lox;

import java.util.Map;
import java.util.HashMap;
import java.util.Objects;


public final class Lexer {
  private static final Map<String, TokenType> KEYWORDS;
  private static final Map<Character, TokenType> SINGLE_CHAR_TOKENS;

  static {
    KEYWORDS = new HashMap<>();
    KEYWORDS.put("and", TokenType.AND);
    KEYWORDS.put("class", TokenType.CLASS);
    KEYWORDS.put("else", TokenType.ELSE);
    KEYWORDS.put("false", TokenType.FALSE);
    KEYWORDS.put("fun", TokenType.FUN);
    KEYWORDS.put("for", TokenType.FOR);
    KEYWORDS.put("if", TokenType.IF);
    KEYWORDS.put("nil", TokenType.NIL);
    KEYWORDS.put("or", TokenType.OR);
    KEYWORDS.put("print", TokenType.PRINT);
    KEYWORDS.put("return", TokenType.RETURN);
    KEYWORDS.put("super", TokenType.SUPER);
    KEYWORDS.put("this", TokenType.THIS);
    KEYWORDS.put("true", TokenType.TRUE);
    KEYWORDS.put("var", TokenType.VAR);
    KEYWORDS.put("while", TokenType.WHILE);

    KEYWORDS.put("!=", TokenType.BANG_EQUAL);
    KEYWORDS.put(">=", TokenType.GREATER_EQUAL);
    KEYWORDS.put("<=", TokenType.LESSER_EQUAL);
    KEYWORDS.put("==", TokenType.EQUAL_EQUAL);
  }

  static {
    SINGLE_CHAR_TOKENS = new HashMap<>();
    SINGLE_CHAR_TOKENS.put(',', TokenType.COMMA);
    SINGLE_CHAR_TOKENS.put('.', TokenType.DOT);
    SINGLE_CHAR_TOKENS.put('-', TokenType.MINUS);
    SINGLE_CHAR_TOKENS.put('+', TokenType.PLUS);
    SINGLE_CHAR_TOKENS.put(';', TokenType.SEMI);
    SINGLE_CHAR_TOKENS.put('/', TokenType.SLASH);
    SINGLE_CHAR_TOKENS.put('*', TokenType.STAR);
    SINGLE_CHAR_TOKENS.put('(', TokenType.LPAR);
    SINGLE_CHAR_TOKENS.put(')', TokenType.RPAR);
    SINGLE_CHAR_TOKENS.put('{', TokenType.LCURL);
    SINGLE_CHAR_TOKENS.put('}', TokenType.RCURL);
  }

  private final String text;
  private int pos = 0;
  private int line = 1;
  private boolean hasErrors = false;

  public Lexer(String text) {
    this.text = text;
  }

  public Token getNextToken() {
    skipWhiteSpace();

    while (hasNext() && isCommentStart()) {
      skipComment();
    }

    skipWhiteSpace();

    if (pos >= text.length()) {
      return new Token(TokenType.EOF, "");
    }

    if (Character.isDigit(getCurrentChar())) {
      return parseNumberToken();
    }

    if (Character.isAlphabetic(getCurrentChar()) || getCurrentChar() == '_') {
      return parseAlphanumericToken();
    }

    if (getCurrentChar() == '"') {
      return parseStringToken();
    }
    return parseSymbolicToken();
  }

  public boolean getHasErrors() {
    return hasErrors;
  }

  char getCurrentChar() {
    if (pos < text.length()) {
      return text.charAt(pos);
    }
    return '\0';
  }

  private char getLookAhead() {
    if (hasNext()) {
      return text.charAt(pos + 1);
    }
    return '\0';
  }

  private boolean hasNext() {
    return pos < text.length();
  }

  private void next() {
    pos++;
  }

  private Token parseNumberToken() {
    StringBuilder sb = new StringBuilder();
    while (hasNext() && Character.isDigit(getCurrentChar())) {
      sb.append(getCurrentChar());
      next();
    }
    if (getCurrentChar() != '.') {
      return new Token(TokenType.NUMBER, sb.toString());
    }
    sb.append(getCurrentChar());
    next();
    while (hasNext() && Character.isDigit(getCurrentChar())) {
      sb.append(getCurrentChar());
      next();
    }
    return new Token(TokenType.NUMBER, sb.toString());
  }

  private Token parseAlphanumericToken() {
    StringBuilder sb = new StringBuilder();

    while (hasNext() && isGoodForId(getCurrentChar())) {
      sb.append(getCurrentChar());
      next();
    }
    return makeMultiSymbolToken(sb.toString());
  }

  private Token parseStringToken() {
    next(); // consume starting quotes
    StringBuilder sb = new StringBuilder();

    while (hasNext() && !currentCharIsStringTerminator()) {
      sb.append(getCurrentChar());
      next();
    }

    if (!hasNext()) {
      return new ErrorToken(
          line,
          getCurrentLine(),
          "Missing closing of the string."
      );
    }
    if (getCurrentChar() == '"') {
      next();
      return new Token(TokenType.STRING, sb.toString());
    }
    ErrorToken et = new ErrorToken(
        line,
        getCurrentLine(),
        "Missing closing of the string."
    );
    next();
    line++;
    return et;
  }

  private Token parseSymbolicToken() {
    TokenType type = SINGLE_CHAR_TOKENS.get(getCurrentChar());
    if (type != null) {
      Token token = new Token(type, Character.toString(getCurrentChar()));
      next();
      return token;
    }

    if (getCurrentChar() == '!') {
      if (getLookAhead() == '=') {
        next();
        next();
        return makeMultiSymbolToken("!=");
      }
      next();
      return new Token(TokenType.BANG, "!");
    }
    if (getCurrentChar() == '=') {
      if (getLookAhead() == '=') {
        next();
        next();
        return makeMultiSymbolToken("==");
      }
      next();
      return new Token(TokenType.EQUAL, "=");
    }
    if (getCurrentChar() == '>') {
      if (getLookAhead() == '=') {
        next();
        next();
        return makeMultiSymbolToken(">=");
      }
      next();
      return new Token(TokenType.GREATER, ">");
    }
    if (getCurrentChar() == '<') {
      if (getLookAhead() == '=') {
        next();
        next();
        return makeMultiSymbolToken("<=");
      }
      next();
      return new Token(TokenType.LESSER, "<");
    }

    Token errorToken = new ErrorToken(
        line,
        getCurrentLine(),
        "Unexpected character: " + getCurrentChar()
    );
    hasErrors = true;
    skipComment();
    return errorToken;
  }

  private void skipWhiteSpace() {
    while (hasNext() && Character.isWhitespace(getCurrentChar())) {
      if (getCurrentChar() == '\n') {
        line++;
      }
      next();
    }
  }

  private void skipComment() {
    while (hasNext() && getCurrentChar() != '\n') {
      next();
    }
    line++;
    next();
  }

  private boolean isGoodForId(char ch) {
    return Character.isAlphabetic(ch) || Character.isDigit(ch) || ch == '_';
  }

  private boolean isCommentStart() {
    return getCurrentChar() == '/' && getLookAhead() == '/';
  }

  private Token makeMultiSymbolToken(String value) {
    TokenType type = KEYWORDS.get(value);
    return new Token(Objects.requireNonNullElse(type, TokenType.ID), value);
  }

  private boolean currentCharIsStringTerminator() {
    char ch = getCurrentChar();
    return ch == '"' || ch == '\n';
  }

  private String getCurrentLine() {
    int i = pos - 1;
    while (i >= 0 && text.charAt(i) != '\n') {
      i--;
    }
    int j = pos;
    while (j < text.length() && text.charAt(j) != '\n') {
      j++;
    }
    return text.substring(i+1, j);
  }
}
