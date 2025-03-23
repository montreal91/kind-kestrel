package org.example.rlc.jvm.middleware

import org.example.rlc.frontend.EnclosedField
import org.example.rlc.frontend.EnclosedLocal
import kotlin.uuid.ExperimentalUuidApi
import org.example.rlc.frontend.Token
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
import org.example.rlc.frontend.scope.EnclosedVariable
import org.example.rlc.frontend.scope.GlobalVariable
import org.example.rlc.frontend.scope.LocalVariable
import org.example.rlc.frontend.scope.UnresolvedVariable
import org.example.rlc.frontend.scope.VariableResolutionResult
import org.example.rlc.frontend.scope.VariableResolutionTable
import org.example.rlc.jvm.ir.BooleanVti
import org.example.rlc.jvm.ir.ByteConstantOperation
import org.example.rlc.jvm.ir.ClassAccessFlags
import org.example.rlc.jvm.ir.ClassFile
import org.example.rlc.jvm.ir.ClassInfo
import org.example.rlc.jvm.ir.CodeAttribute
import org.example.rlc.jvm.ir.ControlFlowOperation
import org.example.rlc.jvm.ir.DoubleValue
import org.example.rlc.jvm.ir.DoubleVti
import org.example.rlc.jvm.ir.EmptyVti
import org.example.rlc.jvm.ir.FieldRefInfo
import org.example.rlc.jvm.ir.IntegerValue
import org.example.rlc.jvm.ir.IntegerVti
import org.example.rlc.jvm.ir.LongVti
import org.example.rlc.jvm.ir.MethodAccessFlags
import org.example.rlc.jvm.ir.MethodInfo
import org.example.rlc.jvm.ir.MethodRefInfo
import org.example.rlc.jvm.ir.MethodSignature
import org.example.rlc.jvm.ir.NameAndTypeInfo
import org.example.rlc.jvm.ir.ObjectVti
import org.example.rlc.jvm.ir.Opcode
import org.example.rlc.jvm.ir.Operation
import org.example.rlc.jvm.ir.OperationWithIndex
import org.example.rlc.jvm.ir.ShortConstantOperation
import org.example.rlc.jvm.ir.SimpleOperation
import org.example.rlc.jvm.ir.StringRefInfo
import org.example.rlc.jvm.ir.VerificationTypeInfo
import org.example.rlc.jvm.ir.javaLangStringObjectVti
import org.example.rlc.jvm.ir.loxMainClassName
import org.example.rlc.jvm.ir.loxObjectClassName
import org.example.rlc.jvm.ir.toUtf8Value


@OptIn(ExperimentalUuidApi::class)
class AstToClassFileIrConverter(private val resolutionTable: VariableResolutionTable) {
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
  private var currentCode = mutableListOf<Operation>()
  private var currentFunction = "Script"
  private var enclosedVariables = mutableMapOf<String, EnclosedVariable>()

  private val methodRefs = mutableMapOf<Token.Type, MethodRefInfo>()
  private val localVariables = mutableListOf<VerificationTypeInfo>()
  private val generatedCallables = mutableMapOf<Int, ClassFile>()

  fun convert(roots: Ast): List<ClassFile> {
    println("==============================")
    println("The Translation stage started.\n")

    initGlobals()
    roots.forEach(this::visitStmt)
    handleCallables()
    finalizeClass()

    println("The Translation stage ended.\n")
    println("============================")

    return classes.toList()
  }

  private fun initGlobals() {
    initClockFunction()
  }

  private fun initClockFunction() {
    generatedCallables[0] = generateAbstractCallable(arity = 0)
    val code = mutableListOf<Operation>()

    code.add(ShortConstantOperation(Opcode.OP_NEW, loxDoubleClassInfo, ObjectVti(loxDoubleClassInfo)))
    code.add(SimpleOperation(Opcode.OP_DUP))

    val systemTimeMillisMri = MethodRefInfo(
      label = "java/lang/System.currentTimeMillis:()J",
      classInfo = ClassInfo(className = "java/lang/System"),
      argsSize = 0,
      returnSize = 2,
      returnTypeInfo = LongVti(),
      nameAndType = NameAndTypeInfo(
        label = "currentTimeMillis:()J",
        name = "currentTimeMillis".toUtf8Value(),
        descriptor = "()J".toUtf8Value()
      )
    )

    val clock = "clock"

    code.add(ShortConstantOperation(Opcode.OP_INVOKE_STATIC, systemTimeMillisMri))
    code.add(SimpleOperation(Opcode.OP_L2D))
    code.add(ShortConstantOperation(Opcode.OP_LDC2_W, DoubleValue(value = "1000.0"), DoubleVti()))
    code.add(SimpleOperation(Opcode.OP_DDIV))
    code.add(ShortConstantOperation(Opcode.OP_INVOKE_SPECIAL, loxDoubleConstructorInfo))
    code.add(SimpleOperation(Opcode.OP_ARETURN))

    classes.add(generateLoxFunction(name = clock, arity = 0, code = code, emptyList()))

    val instantiationCode = listOf(
      ShortConstantOperation(
        Opcode.OP_NEW,
        ClassInfo(className = "LoxFunction$clock"),
        ObjectVti(ClassInfo(className = "LoxFunction$clock"))
      ),
      SimpleOperation(Opcode.OP_DUP),
      ShortConstantOperation(
        Opcode.OP_INVOKE_SPECIAL,
        generateLoxFunctionConstructorInfo("LoxFunction$clock")
      )
    )

    currentCode.addAll(instantiationCode)
    declGlobalVariable(GlobalVariable(clock))
  }

  private fun handleCallables() {
    if (generatedCallables.isNotEmpty()) {
      classes.add(generateLoxBasicCallable())
    }

    classes.addAll(generatedCallables.values)
  }

  private fun finalizeClass() {
    val c = ClassFile(
      thisClassInfo = loxMainClassInfo,
      superClassInfo = objectClass,
      accessFlagList = listOf(ClassAccessFlags.PUBLIC),
      attributeList = listOf(),
      fieldList = listOf(
        dynamicResolutionTableField()
      ),
      interfaceList = listOf(),
      methodList = listOf(
        constructor(),
        loxScriptStaticInitializer(),
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
        declGlobalVariableMethod(),
        getGlobalVariableMethod(),
        setGlobalVariableMethod(),
      )
    )

    classes.add(c)
  }

  private fun visitStmt(stmt: Stmt) = when (stmt) {
    is ExprStmt -> visitExprStmt(stmt)
    is PrintStmt -> visitPrintStmt(stmt)
    is BlockStmt -> visitBlockStmt(stmt)
    is VarDeclStmt -> visitVarDecl(stmt)
    is IfStmt -> visitIfStatement(stmt)
    is WhileStmt -> visitWhileStmt(stmt)
    is ForStmt -> visitForStmt(stmt)
    is FunDeclStmt -> visitFunDeclStmt(stmt)
    is ReturnStmt -> visitReturnStmt(stmt)
  }

  private fun visitExpr(expr: Expr) = when (expr) {
    is Binary -> visitBinary(expr)
    is Grouping -> visitGrouping(expr)
    is Literal -> visitLiteral(expr)
    is Logical -> visitLogical(expr)
    is Unary -> visitUnary(expr)
    is Variable -> visitVariable(expr)
    is Assignment -> visitAssignment(expr)
    is CallExpr -> visitCallExpr(expr)
  }

  private fun visitExprStmt(exprStmt: ExprStmt) {
    visitExpr(exprStmt.expr)
    currentCode.add(SimpleOperation(Opcode.OP_POP))
  }

  private fun visitPrintStmt(printStmt: PrintStmt) {
    val printStreamVti = ObjectVti(ClassInfo(className = "java/io/PrintStream"), isArray = false)
    currentCode.add(ShortConstantOperation(Opcode.OP_GETSTATIC, systemOutField, printStreamVti))
    visitExpr(printStmt.expr)
    currentCode.add(ShortConstantOperation(Opcode.OP_INVOKE_VIRTUAL, printMethodRef))
  }

  private fun visitReturnStmt(stmt: ReturnStmt) {
    stmt.expr?.let(this::visitExpr) ?: compileNil()
    currentCode.add(SimpleOperation(Opcode.OP_ARETURN))
  }

  private fun visitBlockStmt(stmt: BlockStmt) {
    stmt.statements.forEach(this::visitStmt)
  }

  private fun visitVarDecl(stmt: VarDeclStmt) {
    stmt.initializer?.let(this::visitExpr) ?: compileNil()

    when (val resolution = resolutionTable.get(stmt.uid)) {
      is LocalVariable -> storeLocalVariable(resolution)
      is GlobalVariable -> declGlobalVariable(resolution)
      is EnclosedVariable -> {
        println("We can't declare an enclosed variable.")
      }

      UnresolvedVariable -> throw IllegalStateException(
        "Variable ${stmt.variable} is unresolved, but expected to be resolved"
      )
    }
  }

  private fun visitFunDeclStmt(stmt: FunDeclStmt) {
    val outerCode = currentCode
    val outerFunction = currentFunction
    val outerEnclosedVariables = enclosedVariables

    enclosedVariables = mutableMapOf()
    currentCode = mutableListOf()
    currentFunction = stmt.identifier.value

    visitBlockStmt(stmt.body)

    // Well, problem is that fun middle(y) doesn't explicitly reference the variable x.
    // It references the variable x implicitly.
    // Why?
    // Because inner(z) function during its declaration has access to scopes of functions middle and outer
    // X from inner(z) does not reference a local variable, it actually references another enclosed variable

    if (currentCode.isEmpty() || currentCode.last().opcode != Opcode.OP_ARETURN) {
      compileNil()
      currentCode.add(SimpleOperation(Opcode.OP_ARETURN))
    }

    val function = generateLoxFunction(stmt.identifier.value, stmt.arity, currentCode, enclosedVariables.values.toList())
    classes.add(function)

    if (!generatedCallables.containsKey(stmt.arity)) {
      generatedCallables[stmt.arity] = generateAbstractCallable(stmt.arity)
    }

    currentCode = outerCode
    currentFunction = outerFunction

    val instantiationCode =
      generateFunctionInstantiationCode(loxObjectClassInfo, stmt.identifier.value, enclosedVariables.values.toList())

    enclosedVariables = outerEnclosedVariables
    currentCode.addAll(instantiationCode)

    when (val resolution = resolutionTable.get(stmt.uid)) {
      is GlobalVariable -> declGlobalVariable(resolution)
      is LocalVariable -> storeLocalVariable(resolution)
      is EnclosedVariable -> {
        println("A function can't be declared as an enclosed value.")
      }

      UnresolvedVariable -> throw IllegalStateException(
        "Variable ${stmt.identifier} is unresolved, but expected to be resolved"
      )
    }
  }

  private fun visitIfStatement(stmt: IfStmt) {
    visitExpr(stmt.expr)

    currentCode.add(ShortConstantOperation(Opcode.OP_INVOKE_VIRTUAL, loxObjectTruthyMri))
    currentCode.add(ShortConstantOperation(Opcode.OP_CHECKCAST, loxBooleanClassInfo))
    currentCode.add(
      ShortConstantOperation(
        Opcode.OP_GETFIELD,
        booleanValueFieldRefInfo,
        BooleanVti()
      )
    )

    val branch = ControlFlowOperation(Opcode.OP_IFEQ, jumpTo = -1)
    currentCode.add(branch)
    visitStmt(stmt.ifBranch)
    branch.setJumpTo(currentCode.size)

    stmt.elseBranch?.let {
      val jump = ControlFlowOperation(Opcode.OP_GOTO, jumpTo = -1)
      currentCode.add(jump)
      branch.setJumpTo(currentCode.size)
      visitStmt(stmt.elseBranch)
      jump.setJumpTo(currentCode.size)
    }
  }

  private fun visitWhileStmt(stmt: WhileStmt) {
    val loopBack = currentCode.size

    visitExpr(stmt.expr)

    currentCode.add(ShortConstantOperation(Opcode.OP_INVOKE_VIRTUAL, loxObjectTruthyMri))
    currentCode.add(ShortConstantOperation(Opcode.OP_CHECKCAST, loxBooleanClassInfo))
    currentCode.add(
      ShortConstantOperation(
        Opcode.OP_GETFIELD,
        booleanValueFieldRefInfo,
        BooleanVti()
      )
    )

    val exitLoop = ControlFlowOperation(Opcode.OP_IFEQ, jumpTo = -1)
    currentCode.add(exitLoop)

    visitStmt(stmt.body)
    currentCode.add(ControlFlowOperation(Opcode.OP_GOTO, jumpTo = loopBack))
    exitLoop.setJumpTo(currentCode.size)
  }

  private fun visitForStmt(stmt: ForStmt) {
    stmt.initStmt?.let { visitStmt(stmt.initStmt) }
    val loopBack = currentCode.size
    val exitLoop = ControlFlowOperation(Opcode.OP_IFEQ, jumpTo = -1)

    if (stmt.conditionExpr != null) {
      visitExpr(stmt.conditionExpr)
      currentCode.add(ShortConstantOperation(Opcode.OP_INVOKE_VIRTUAL, loxObjectTruthyMri))
      currentCode.add(ShortConstantOperation(Opcode.OP_CHECKCAST, loxBooleanClassInfo))
      currentCode.add(
        ShortConstantOperation(
          Opcode.OP_GETFIELD,
          booleanValueFieldRefInfo,
          BooleanVti()
        )
      )

      currentCode.add(exitLoop)
    } else {
      currentCode.add(SimpleOperation(Opcode.OP_ICONST_1))
      currentCode.add(exitLoop)
    }

    visitStmt(stmt.body)

    stmt.updateExpr?.let {
      visitExpr(stmt.updateExpr)
      currentCode.add(SimpleOperation(Opcode.OP_POP))
    }

    currentCode.add(ControlFlowOperation(Opcode.OP_GOTO, jumpTo = loopBack))
    exitLoop.setJumpTo(currentCode.size)
  }

  private fun visitCallExpr(expr: CallExpr) {
    println("Visiting Call Expression. ${expr.args.size}")
    visitExpr(expr.callee)

    currentCode.add(SimpleOperation(Opcode.OP_DUP, ObjectVti(loxObjectClassInfo)))
    currentCode.add(ByteConstantOperation(Opcode.OP_LDC, IntegerValue(expr.args.size), IntegerVti()))
    currentCode.add(ShortConstantOperation(Opcode.OP_INVOKE_STATIC, generateCallCheckMethodRef()))
    currentCode.add(ShortConstantOperation(Opcode.OP_CHECKCAST, ClassInfo(className = "LoxCallable${expr.args.size}")))

    expr.args.forEach(this::visitExpr)

    currentCode.add(ShortConstantOperation(Opcode.OP_INVOKE_VIRTUAL, generateCallMethodRef(expr.args.size)))
  }

  private fun visitLiteral(expr: Literal) = when (expr.type) {
    Literal.Type.NUMBER -> compileNumber(expr)
    Literal.Type.BOOLEAN -> compileBoolean(expr)
    Literal.Type.STRING -> compileString(expr)
    Literal.Type.NIL_TYPE -> compileNil()
  }

  private fun visitVariable(expr: Variable) {
    when (val resolution = resolutionTable.get(expr.uid)) {
      is LocalVariable -> currentCode.add(
        OperationWithIndex(
          Opcode.OP_ALOAD,
          getActualLocalVariableIndex(resolution).toByte(),
          ObjectVti(loxObjectClassInfo)
        )
      )

      is EnclosedVariable -> {
        println("Accessing an enclosed variable ${expr.variable}")

        val op = ShortConstantOperation(
          Opcode.OP_GETFIELD,
          generateEnclosedFieldRef(className = "LoxFunction$currentFunction", fieldName = resolution.name),
          ObjectVti(loxObjectClassInfo)
        )

        currentCode.add(SimpleOperation(Opcode.OP_ALOAD_0))
        currentCode.add(op)
        enclosedVariables[resolution.name] = resolution
      }

      is GlobalVariable -> getGlobalVariable(resolution)
      UnresolvedVariable -> throw IllegalStateException(
        "Variable ${expr.variable} is unresolved, but expected to be resolved"
      )
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
      Token.Type.AND -> compileLogical(expr)
      Token.Type.OR -> compileLogical(expr)
      else -> throw IllegalStateException("Unsupported logical operator [${expr.operator}].")
    }
  }

  private fun storeLocalVariable(resolution: LocalVariable) {
    val op = OperationWithIndex(
      Opcode.OP_ASTORE,
      getActualLocalVariableIndex(resolution).toByte(),
      ObjectVti(loxObjectClassInfo)
    )

    if (resolution.variableArrayIndex >= localVariables.size) {
      localVariables.add(ObjectVti(loxObjectClassInfo))
    }

    currentCode.add(op)
  }

  private fun setGlobalVariable(variable: GlobalVariable) {
    val getGlobalMethodRef = "(LLoxObject;Ljava/lang/String;Ljava/lang/String;)V"
    val getMethodRef = MethodRefInfo(
      label = "LoxScript.__set_global__:$getGlobalMethodRef",
      classInfo = ClassInfo(className = "LoxScript"),
      nameAndType = NameAndTypeInfo(
        label = "__set_global__:$getGlobalMethodRef",
        name = "__set_global__".toUtf8Value(),
        descriptor = getGlobalMethodRef.toUtf8Value(),
      ),
      argsSize = 3,
      returnSize = 0,
      returnTypeInfo = EmptyVti()
    )

    val message = "Undefined variable '${variable.name}'."
    val ops = listOf(
      ByteConstantOperation(Opcode.OP_LDC, StringRefInfo(variable.name), javaLangStringObjectVti),
      ByteConstantOperation(Opcode.OP_LDC, StringRefInfo(value = message), javaLangStringObjectVti),
      ShortConstantOperation(Opcode.OP_INVOKE_STATIC, getMethodRef)
    )

    currentCode.addAll(ops)
  }

  private fun declGlobalVariable(variable: GlobalVariable) {
    val getGlobalMethodRef = "(LLoxObject;Ljava/lang/String;)V"
    val getMethodRef = MethodRefInfo(
      label = "LoxScript.__decl_global__:$getGlobalMethodRef",
      classInfo = ClassInfo(className = "LoxScript"),
      nameAndType = NameAndTypeInfo(
        label = "__decl_global__:$getGlobalMethodRef",
        name = "__decl_global__".toUtf8Value(),
        descriptor = getGlobalMethodRef.toUtf8Value(),
      ),
      argsSize = 2,
      returnSize = 0,
      returnTypeInfo = EmptyVti()
    )

    currentCode.add(
      ByteConstantOperation(Opcode.OP_LDC, StringRefInfo(variable.name), javaLangStringObjectVti)
    )
    currentCode.add(ShortConstantOperation(Opcode.OP_INVOKE_STATIC, getMethodRef))
  }

  private fun getGlobalVariable(variable: GlobalVariable) {
    val getGlobalMethodRef = "(Ljava/lang/String;Ljava/lang/String;)LLoxObject;"
    val getMethodRef = MethodRefInfo(
      label = "LoxScript.__get_global__:$getGlobalMethodRef",
      classInfo = ClassInfo(className = "LoxScript"),
      nameAndType = NameAndTypeInfo(
        label = "__get_global__:$getGlobalMethodRef",
        name = "__get_global__".toUtf8Value(),
        descriptor = getGlobalMethodRef.toUtf8Value(),
      ),
      argsSize = 2,
      returnSize = 1,
      returnTypeInfo = BooleanVti()
    )

    val message = "Undefined variable '${variable.name}'."
    currentCode.add(ByteConstantOperation(Opcode.OP_LDC, StringRefInfo(variable.name), javaLangStringObjectVti))
    currentCode.add(ByteConstantOperation(Opcode.OP_LDC, StringRefInfo(value = message), javaLangStringObjectVti))
    currentCode.add(ShortConstantOperation(Opcode.OP_INVOKE_STATIC, getMethodRef))
  }

  private fun compileLogical(expr: Logical) {
    println("Compiling Logical: ${expr.operator.type}")
    currentCode.add(SimpleOperation(Opcode.OP_DUP))
    currentCode.add(ShortConstantOperation(Opcode.OP_INVOKE_VIRTUAL, loxObjectTruthyMri))
    currentCode.add(ShortConstantOperation(Opcode.OP_CHECKCAST, loxBooleanClassInfo))
    currentCode.add(
      ShortConstantOperation(
        Opcode.OP_GETFIELD,
        booleanValueFieldRefInfo,
        BooleanVti()
      )
    )

    val opcode = when (expr.operator.type) {
      Token.Type.AND -> Opcode.OP_IFEQ
      Token.Type.OR -> Opcode.OP_IFNE
      else -> throw IllegalStateException("Unsupported logical operator [${expr.operator}].")
    }

    val branch = ControlFlowOperation(opcode, jumpTo = -1)
    currentCode.add(branch)
    currentCode.add(SimpleOperation(Opcode.OP_POP))

    visitExpr(expr.right)

    branch.setJumpTo(currentCode.size)
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

  private fun visitAssignment(expr: Assignment) {
    visitExpr(expr.right)
    currentCode.add(SimpleOperation(Opcode.OP_DUP))

    if (expr.left is Variable) {
      when (val resolution = resolutionTable.get(expr.left.uid)) {
        is LocalVariable -> storeLocalVariable(resolution)
        is GlobalVariable -> setGlobalVariable(resolution)
        is EnclosedVariable -> {
          println("Assigning an enclosed variable ${expr.left}")
          val op = ShortConstantOperation(
            Opcode.OP_PUTFIELD,
            generateEnclosedFieldRef(className = "LoxFunction$currentFunction", fieldName = resolution.name)
          )

          currentCode.add(SimpleOperation(Opcode.OP_ALOAD_0))
          currentCode.add(SimpleOperation(Opcode.OP_SWAP))
          currentCode.add(op)
          enclosedVariables[resolution.name] = resolution
        }

        UnresolvedVariable -> {}
      }
    }
  }

  private fun compileBoolean(expr: Literal) {
    val valueOp = when (expr.value) {
      "true" -> Opcode.OP_ICONST_1
      "false" -> Opcode.OP_ICONST_0
      else -> throw RuntimeException("Unsupported boolean literal: [${expr.value}]")
    }

    val ops = listOf(
      ShortConstantOperation(Opcode.OP_NEW, loxBooleanClassInfo, ObjectVti(loxObjectClassInfo)),
      SimpleOperation(Opcode.OP_DUP),
      SimpleOperation(valueOp),
      ShortConstantOperation(Opcode.OP_INVOKE_SPECIAL, loxBooleanConstructorInfo)
    )

    currentCode.addAll(ops)
  }

  private fun compileNumber(literal: Literal) {
    val ops = listOf(
      ShortConstantOperation(Opcode.OP_NEW, loxDoubleClassInfo, ObjectVti(loxObjectClassInfo)),
      SimpleOperation(Opcode.OP_DUP),
      ShortConstantOperation(Opcode.OP_LDC2_W, DoubleValue(literal.value), DoubleVti()),
      ShortConstantOperation(Opcode.OP_INVOKE_SPECIAL, loxDoubleConstructorInfo)
    )

    currentCode.addAll(ops)
  }

  private fun compileNil() {
    val ops = listOf(
      ShortConstantOperation(Opcode.OP_NEW, loxNilClassInfo, ObjectVti(loxObjectClassInfo)),
      SimpleOperation(Opcode.OP_DUP),
      ShortConstantOperation(Opcode.OP_INVOKE_SPECIAL, loxNilConstructorInfo),
    )

    currentCode.addAll(ops)
  }

  private fun compileString(literal: Literal) {
    val ops = listOf(
      ShortConstantOperation(Opcode.OP_NEW, loxStringClassInfo, ObjectVti(loxObjectClassInfo)),
      SimpleOperation(Opcode.OP_DUP),
      ByteConstantOperation(Opcode.OP_LDC, StringRefInfo(literal.value), javaLangStringObjectVti),
      ShortConstantOperation(Opcode.OP_INVOKE_SPECIAL, loxStringConstructorInfo)
    )

    currentCode.addAll(ops)
  }

  private fun getLocalVariableArraySize() = resolutionTable.getMaxIndex() + 2

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
          attributes = listOf(),
          maxLocals2 = getLocalVariableArraySize()
        )
      ),
      isStatic = true,
      signature = MethodSignature(
        listOf(ObjectVti(isArray = true, classInfo = javaLangStringArrayClassInfo)),
        EmptyVti()
      ),
      localVariables = localVariables.toList(),
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
          attributes = listOf(),
          maxLocals2 = 1
        )
      ),
      isStatic = true,
      signature = MethodSignature(listOf(), EmptyVti()),
      localVariables = listOf(ObjectVti(javaLangObjectClassInfo))
    )
  }

  private fun getArithmeticMethodRef(operation: Token.Type): MethodRefInfo {
    return methodRefs.getOrPut(operation) { createBinaryNumericMethodRef(operation) }
  }

  private fun createBinaryNumericMethodRef(operation: Token.Type): MethodRefInfo {
    val nameAndType = binaryOperations[operation]
    val label = loxMainClassName + "." + nameAndType!!.label

    return MethodRefInfo(
      label = label,
      classInfo = loxMainClassInfo,
      nameAndType = nameAndType,
      argsSize = 2,
      returnSize = 1,
      returnTypeInfo = ObjectVti(loxObjectClassInfo, isArray = false)
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
      returnSize = 1,
      returnTypeInfo = ObjectVti(loxObjectClassInfo, isArray = false)
    )
  }
}

// Maybe I should put all such functions to some other place later.
private fun generateLoxFunctionConstructorInfo(className: String) = MethodRefInfo(
  label = "${className}.\"<init>\":()V",
  classInfo = ClassInfo(className),
  nameAndType = NameAndTypeInfo(
    label = "\"<init>\":()V",
    name = "<init>".toUtf8Value(),
    descriptor = "()V".toUtf8Value(),
  ),
  argsSize = 1,
  returnSize = 0,
  returnTypeInfo = EmptyVti(),
)

private fun generateCallCheckMethodRef(): MethodRefInfo {
  val className = "LoxBasicCallable"
  val functionName = "__call_check__"
  return MethodRefInfo(
    label = "$className.$functionName:(L$loxObjectClassName;I)V",
    classInfo = ClassInfo(className),
    nameAndType = NameAndTypeInfo(
      label = "$functionName:(L$loxObjectClassName;I)V",
      name = functionName.toUtf8Value(),
      descriptor = "(L$loxObjectClassName;I)V".toUtf8Value(),
    ),
    argsSize = 2,
    returnSize = 0,
    returnTypeInfo = EmptyVti(),
  )
}

private fun generateCallMethodRef(arity: Int): MethodRefInfo {
  val className = "LoxCallable$arity"
  val functionName = "__call__"

  val sb = StringBuilder()
  sb.append("(")
  for (i in 0..<arity) {
    sb.append("L$loxObjectClassName;")
  }
  sb.append(")L$loxObjectClassName;")
  val signature = sb.toString()
  return MethodRefInfo(
    label = "$className.$functionName:${signature}",
    classInfo = ClassInfo(className),
    nameAndType = NameAndTypeInfo(
      label = "$functionName:${signature}",
      name = functionName.toUtf8Value(),
      descriptor = signature.toUtf8Value(),
    ),
    argsSize = arity + 1,
    returnSize = 1,
    returnTypeInfo = ObjectVti(loxObjectClassInfo)
  )
}

private fun generateEnclosedFieldRef(className: String, fieldName: String): FieldRefInfo {
  return FieldRefInfo(
    label = "$className.__enclosed_value__$fieldName:LLoxObject;",
    classInfo = ClassInfo(className),
    nameAndType = NameAndTypeInfo(
      label = "__enclosed_value__$fieldName:LLoxObject;",
      name = "__enclosed_value__$fieldName".toUtf8Value(),
      descriptor = "LLoxObject;".toUtf8Value()
    )
  )
}

private fun getActualLocalVariableIndex(resolution: VariableResolutionResult) = when (resolution) {
  is EnclosedVariable -> when (resolution.enclosedObject) {
    is EnclosedLocal -> resolution.enclosedObject.localVariableIndex + 1
    is EnclosedField -> -1
  }

  is GlobalVariable -> 0 - 1
  is LocalVariable -> resolution.variableArrayIndex + 1
  UnresolvedVariable -> throw IllegalArgumentException("A variable should be resolved")
}

private fun generateFunctionInstantiationCode(
  thisClassInfo: ClassInfo,
  functionName: String,
  enclosedVariables: List<EnclosedVariable>
): List<Operation> {
  val functionClassName = "LoxFunction${functionName}"
  val thisClassInfo = ClassInfo(functionClassName)
  val res = mutableListOf(
    ShortConstantOperation(Opcode.OP_NEW, thisClassInfo, ObjectVti(thisClassInfo)),
    SimpleOperation(Opcode.OP_DUP),
    ShortConstantOperation(
      Opcode.OP_INVOKE_SPECIAL,
      generateLoxFunctionConstructorInfo(className = "LoxFunction${functionName}")
    )
  )

  for (v in enclosedVariables) {
    res.addAll(
      listOf(
        SimpleOperation(Opcode.OP_DUP),
        OperationWithIndex(
          Opcode.OP_ALOAD,
          getActualLocalVariableIndex(v).toByte(),
          ObjectVti(loxObjectClassInfo)
        ),
        ShortConstantOperation(
          Opcode.OP_PUTFIELD,
          generateEnclosedFieldRef(className = "LoxFunction${functionName}", fieldName = v.name)
        )
      )
    )
  }

  return res.toList()
}
