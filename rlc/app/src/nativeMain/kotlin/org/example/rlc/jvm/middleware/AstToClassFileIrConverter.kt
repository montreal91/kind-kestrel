package org.example.rlc.jvm.middleware

import org.example.rlc.frontend.Token
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
import org.example.rlc.frontend.scope.UnresolvedVariable
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
import org.example.rlc.jvm.ir.loxMainClassName
import org.example.rlc.jvm.ir.loxObjectClassName
import org.example.rlc.jvm.ir.toUtf8Value
import kotlin.uuid.ExperimentalUuidApi


@OptIn(markerClass = [ExperimentalUuidApi::class])
class AstToClassFileIrConverter(private val resolutionTable: VariableResolutionTable) {
  private val objectClass = javaLangObjectClassInfo
  private val classes = mutableListOf(
    loxClass(),
    loxObjectCf(),
    loxBooleanCf(),
    loxDoubleCf(),
    LoxNil.loxNilCf(),
    loxStringCf(),
    loxRuntimeError(),
    loxPointerCf(),
  )
  private var currentCode = mutableListOf<Operation>()
  private var currentFunction = "Script"

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

    classes.add(
      generateLoxFunction(
        name = clock,
        arity = 0,
        code = code,
        enclosedVariables = emptyList(),
        isMethod = false
      )
    )

    val instantiationCode = listOf(
      ShortConstantOperation(
        Opcode.OP_NEW,
        ClassInfo(className = "LoxFunction_$clock"),
        ObjectVti(ClassInfo(className = "LoxFunction_$clock"))
      ),
      SimpleOperation(Opcode.OP_DUP),
      ShortConstantOperation(
        Opcode.OP_INVOKE_SPECIAL,
        LoxFunction.generateConstructorInfo("LoxFunction_$clock")
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
    is ExprStmt -> visitExprStmt(exprStmt = stmt)
    is PrintStmt -> visitPrintStmt(printStmt = stmt)
    is BlockStmt -> visitBlockStmt(stmt)
    is VarDeclStmt -> visitVarDecl(stmt)
    is IfStmt -> visitIfStatement(stmt)
    is WhileStmt -> visitWhileStmt(stmt)
    is ForStmt -> visitForStmt(stmt)
    is FunDeclStmt -> visitFunDeclStmt(funDecl = stmt, isMethod = false, prefix = "")
    is ReturnStmt -> visitReturnStmt(stmt)
    is ClassDeclStmt -> visitClassDeclStmt(classDeclStmt = stmt)
  }

  private fun visitExpr(expr: Expr) = when (expr) {
    is Binary -> visitBinary(expr)
    is Grouping -> visitGrouping(expr)
    is Literal -> visitLiteral(expr)
    is Logical -> visitLogical(expr)
    is Unary -> visitUnary(expr)
    is Variable -> visitVariable(expr)
    is Assign -> visitAssignment(expr)
    is Call -> visitCallExpr(expr)
    is Get -> visitGet(get = expr)
    is Set -> visitSet(set = expr)
    is This -> visitThis()
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

    // This actually stores LoxPointer inside global or local variable
    when (val resolution = resolutionTable.get(stmt.uid)) {
      is LocalVariable -> declLocalVariable(resolution)
      is GlobalVariable -> declGlobalVariable(resolution)
      is EnclosedVariable -> {
        println("We can't declare an enclosed variable.")
      }

      UnresolvedVariable -> throw IllegalStateException(
        "Variable ${stmt.variable} is unresolved, but expected to be resolved"
      )
    }
  }

  private fun visitFunDeclStmt(funDecl: FunDeclStmt, isMethod: Boolean, prefix: String) {
    println("Compiling function. (function=${funDecl.identifier.value})")
    val outerCode = currentCode
    val outerFunction = currentFunction

    currentCode = mutableListOf()
    currentFunction = funDecl.identifier.value

    visitBlockStmt(funDecl.body)

    if (currentCode.isEmpty() || currentCode.last().opcode != Opcode.OP_ARETURN) {
      compileNil()
      currentCode.add(SimpleOperation(Opcode.OP_ARETURN))
    }

    val function = generateLoxFunction(
      prefix + funDecl.identifier.value,
      funDecl.arity,
      currentCode,
      funDecl.enclosedVariables.map { it as EnclosedVariable },
      isMethod = isMethod
    )

    classes.add(function)

    if (!generatedCallables.containsKey(funDecl.arity)) {
      generatedCallables[funDecl.arity] = generateAbstractCallable(funDecl.arity)
    }

    currentCode = outerCode
    currentFunction = outerFunction

    when (isMethod) {
      true -> {}
      false -> {
        val instantiationCode = LoxFunction.generateInstantiationCode(
          functionName = "LoxFunction_${funDecl.identifier.value}",
          outerFunctionName = outerFunction,
          enclosedVariables = funDecl.enclosedVariables.map { it as EnclosedVariable },
          currentCodeOffset = currentCode.size,
        )

        currentCode.addAll(instantiationCode)
      }
    }

    if (isMethod) {
      return
    }

    when (val resolution = resolutionTable.get(funDecl.uid)) {
      is GlobalVariable -> declGlobalVariable(resolution)
      is LocalVariable -> setLocalVariable(resolution)
      is EnclosedVariable -> {
        println("A function can't be declared as an enclosed value.")
      }

      UnresolvedVariable -> throw IllegalStateException(
        "Variable ${funDecl.identifier} is unresolved, but expected to be resolved"
      )
    }

    println("Finished compiling function. (function=${funDecl.identifier.value})")
  }

  private fun visitClassDeclStmt(classDeclStmt: ClassDeclStmt) {
    classes.add(generateInstanceClass(name = classDeclStmt.identifier.value))

    classDeclStmt.methods.forEach { stmt -> visitFunDeclStmt(stmt, true, prefix = classDeclStmt.identifier.value + "_") }

    val functionStuff = classDeclStmt.methods.map {
      stmt -> FunctionStuff(
        name = stmt.identifier.value,
        enclosedVariables = stmt.enclosedVariables.map{ it as EnclosedVariable }.toList()
      )
    }

    val constructor = generateConstructorClass(
      name = classDeclStmt.identifier.value,
      functionStuff = functionStuff,
      arity = classDeclStmt.getConstructorArity()
    )

    classes.add(constructor)

    /// Constructor method initialization code goes here
    currentCode.addAll(LoxFunction.generateInstantiationCode(
      functionName = constructor.thisClassInfo.className.encodedString,
      outerFunctionName = "",
      enclosedVariables = listOf(),
      currentCodeOffset = currentCode.size,
    ))

    when (val resolution = resolutionTable.get(classDeclStmt.uid)) {
      is GlobalVariable -> declGlobalVariable(resolution)
      is LocalVariable -> setLocalVariable(resolution)
      is EnclosedVariable -> {
        println("A function can't be declared as an enclosed value.")
      }

      UnresolvedVariable -> throw IllegalStateException(
        "Variable ${classDeclStmt.identifier} is unresolved, but expected to be resolved"
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

  private fun visitCallExpr(expr: Call) {
    println("Visiting Call Expression. ${expr.args.size}")
    visitExpr(expr.callee)

    currentCode.add(SimpleOperation(Opcode.OP_DUP, ObjectVti(loxObjectClassInfo)))
    currentCode.add(ByteConstantOperation(Opcode.OP_LDC, IntegerValue(expr.args.size), IntegerVti()))
    currentCode.add(ShortConstantOperation(Opcode.OP_INVOKE_STATIC, generateCallCheckMethodRef()))
    currentCode.add(ShortConstantOperation(Opcode.OP_CHECKCAST, ClassInfo(className = "LoxCallable${expr.args.size}")))

    expr.args.forEach(this::visitExpr)

    currentCode.add(ShortConstantOperation(Opcode.OP_INVOKE_VIRTUAL, LoxFunction.generateCallMethodRef(expr.args.size)))
  }

  private fun visitGet(get: Get) {
    visitExpr(expr = get.obj)

    val loadFieldNameOp = ByteConstantOperation(
      Opcode.OP_LDC,
      constant = StringRefInfo(value = get.name),
      value = JavaString.VERIFICATION_TYPE,
    )

    val loadErrorMessageOp = ByteConstantOperation(
      Opcode.OP_LDC,
      constant = StringRefInfo(value = "Undefined property ${get.name}."),
      value = JavaString.VERIFICATION_TYPE,
    )

    val invokeInstanceGetMethodOp = ShortConstantOperation(
      Opcode.OP_INVOKE_VIRTUAL,
      constant = getInstanceFieldMethodRef(),
    )

    currentCode.add(loadFieldNameOp)
    currentCode.add(loadErrorMessageOp)
    currentCode.add(invokeInstanceGetMethodOp)
  }

  private fun visitSet(set: Set) {
    visitExpr(expr = set.obj)
    visitExpr(expr = set.value)

    val loadFieldNameOp = ByteConstantOperation(
      Opcode.OP_LDC,
      constant = StringRefInfo(value = set.name),
      value = JavaString.VERIFICATION_TYPE,
    )

    val invokeSetMethodOp = ShortConstantOperation(
      Opcode.OP_INVOKE_VIRTUAL,
      constant = setInstanceFieldMethodRef()
    )

    currentCode.add(loadFieldNameOp)
    currentCode.add(invokeSetMethodOp)
  }

  private fun visitLiteral(expr: Literal) = when (expr.type) {
    Literal.Type.NUMBER -> compileNumber(expr)
    Literal.Type.BOOLEAN -> compileBoolean(expr)
    Literal.Type.STRING -> compileString(expr)
    Literal.Type.NIL_TYPE -> compileNil()
  }

  private fun visitVariable(expr: Variable) {
    when (val resolution = resolutionTable.get(expr.uid)) {
      is LocalVariable -> getLocalVariable(resolution)
      is EnclosedVariable -> getEnclosedVariable(resolution)
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

  private fun visitThis() {
    currentCode.add(SimpleOperation(Opcode.OP_ALOAD_0))
    currentCode.add(
      ShortConstantOperation(
        Opcode.OP_GETFIELD,
        JavaClass.generateFieldRef("LoxBasicCallable", "__this__"),
        loxObjectVti,
      )
    )
  }

  private fun setLocalVariable(resolution: LocalVariable) {
    val variableArrayIndex = resolution.actualIndex().toByte()

    if (resolution.isUpValue) {
      currentCode.add(OperationWithIndex(Opcode.OP_ALOAD, variableArrayIndex, loxObjectVti))
      currentCode.add(ShortConstantOperation(Opcode.OP_CHECKCAST, ClassInfo(className = "LoxPointer")))
      currentCode.add(SimpleOperation(Opcode.OP_SWAP))
      currentCode.add(
        ShortConstantOperation(
          Opcode.OP_PUTFIELD,
          FieldRefInfo(className = "LoxPointer", fieldName = "__value__", fieldType = "LoxObject")
        )
      )
      return
    }

    val op = OperationWithIndex(
      Opcode.OP_ASTORE,
      variableArrayIndex,
      ObjectVti(loxObjectClassInfo)
    )

    if (resolution.variableArrayIndex >= localVariables.size) {
      localVariables.add(loxObjectVti)
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
      ByteConstantOperation(Opcode.OP_LDC, StringRefInfo(variable.name), JavaString.VERIFICATION_TYPE),
      ByteConstantOperation(Opcode.OP_LDC, StringRefInfo(value = message), JavaString.VERIFICATION_TYPE),
      ShortConstantOperation(Opcode.OP_INVOKE_STATIC, getMethodRef)
    )

    currentCode.addAll(ops)
  }

  private fun setEnclosedVariable(variable: EnclosedVariable) {
    println("Assigning an enclosed variable $variable")
    val getFieldOp = ShortConstantOperation(
      Opcode.OP_GETFIELD,
      JavaClass.generateFieldRef(
        className = "LoxFunction$currentFunction",
        fieldName = "__enclosed_value__${variable.name}__",
      ),
      loxObjectVti,
    )

    currentCode.add(SimpleOperation(Opcode.OP_ALOAD_0))
    currentCode.add(getFieldOp)
    currentCode.add(ShortConstantOperation(Opcode.OP_CHECKCAST, ClassInfo(className = "LoxPointer")))
    currentCode.add(SimpleOperation(Opcode.OP_SWAP))
    currentCode.add(
      ShortConstantOperation(
        Opcode.OP_PUTFIELD,
        FieldRefInfo(className = "LoxPointer", fieldName = "__value__", fieldType = "LoxObject")
      )
    )
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
      ByteConstantOperation(Opcode.OP_LDC, StringRefInfo(variable.name), JavaString.VERIFICATION_TYPE)
    )
    currentCode.add(ShortConstantOperation(Opcode.OP_INVOKE_STATIC, getMethodRef))
  }

  private fun declLocalVariable(variable: LocalVariable) {
    println("Declaring local variable in index: $variable")
    val variableArrayIndex = variable.actualIndex().toByte()
    val op = OperationWithIndex(
      Opcode.OP_ASTORE,
      variableArrayIndex,
      ObjectVti(loxObjectClassInfo)
    )

    if (variable.variableArrayIndex >= localVariables.size) {
      localVariables.add(loxObjectVti)
    }

    currentCode.add(op)
    if (variable.isUpValue) {
      val extraWork = mutableListOf<Operation>()
      extraWork.add(ShortConstantOperation(Opcode.OP_NEW, ClassInfo(className = "LoxPointer"), loxObjectVti))
      extraWork.add(SimpleOperation(Opcode.OP_DUP))
      extraWork.add(
        ShortConstantOperation(
          Opcode.OP_INVOKE_SPECIAL,
          LoxFunction.generateConstructorInfo(className = "LoxPointer"),
        )
      )
      extraWork.add(SimpleOperation(Opcode.OP_DUP))
      extraWork.add(OperationWithIndex(Opcode.OP_ALOAD, variableArrayIndex, loxObjectVti))
      extraWork.add(
        ShortConstantOperation(
          Opcode.OP_PUTFIELD,
          JavaClass.generateFieldRef(className = "LoxPointer", fieldName = "__value__")
        )
      )
      extraWork.add(OperationWithIndex(Opcode.OP_ASTORE, variableArrayIndex, ObjectVti(loxObjectClassInfo)))

      currentCode.addAll(extraWork)
    }
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
    currentCode.add(ByteConstantOperation(Opcode.OP_LDC, StringRefInfo(variable.name), JavaString.VERIFICATION_TYPE))
    currentCode.add(ByteConstantOperation(Opcode.OP_LDC, StringRefInfo(value = message), JavaString.VERIFICATION_TYPE))
    currentCode.add(ShortConstantOperation(Opcode.OP_INVOKE_STATIC, getMethodRef))
  }

  private fun getLocalVariable(variable: LocalVariable) {
    println("Compiling access to local variable (variable=$variable function=$currentFunction)")
    currentCode.add(
      OperationWithIndex(Opcode.OP_ALOAD, variable.actualIndex().toByte(), ObjectVti(loxObjectClassInfo))
    )

    if (variable.isUpValue) {
      val pointerValueFri = FieldRefInfo(
        className = "LoxPointer",
        fieldName = "__value__",
        fieldType = loxObjectClassName
      )

      currentCode.add(ShortConstantOperation(Opcode.OP_CHECKCAST, ClassInfo(className = "LoxPointer")))
      currentCode.add(ShortConstantOperation(Opcode.OP_GETFIELD, pointerValueFri, loxObjectVti))
    }
  }

  private fun getEnclosedVariable(variable: EnclosedVariable) {
    println("Accessing an enclosed variable ${variable.name}")

    val op = ShortConstantOperation(
      Opcode.OP_GETFIELD,
      JavaClass.generateFieldRef(
        className = "LoxFunction$currentFunction",
        fieldName = "__enclosed_value__${variable.name}__"
      ),
      ObjectVti(loxObjectClassInfo)
    )

    currentCode.add(SimpleOperation(Opcode.OP_ALOAD_0))
    currentCode.add(op)

    val pointerValueFri = FieldRefInfo(
      className = "LoxPointer",
      fieldName = "__value__",
      fieldType = loxObjectClassName
    )

    currentCode.add(ShortConstantOperation(Opcode.OP_CHECKCAST, ClassInfo(className = "LoxPointer")))
    currentCode.add(ShortConstantOperation(Opcode.OP_GETFIELD, pointerValueFri, loxObjectVti))
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

  private fun visitAssignment(expr: Assign) {
    visitExpr(expr.right)
    currentCode.add(SimpleOperation(Opcode.OP_DUP))

    if (expr.left is Variable) {
      when (val resolution = resolutionTable.get(expr.left.uid)) {
        is LocalVariable -> setLocalVariable(resolution)
        is GlobalVariable -> setGlobalVariable(resolution)
        is EnclosedVariable -> setEnclosedVariable(resolution)
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
    currentCode.addAll(elements = LoxNil.generateNil())
  }

  private fun compileString(literal: Literal) {
    val ops = listOf(
      ShortConstantOperation(Opcode.OP_NEW, loxStringClassInfo, ObjectVti(loxObjectClassInfo)),
      SimpleOperation(Opcode.OP_DUP),
      ByteConstantOperation(Opcode.OP_LDC, StringRefInfo(literal.value), JavaString.VERIFICATION_TYPE),
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
