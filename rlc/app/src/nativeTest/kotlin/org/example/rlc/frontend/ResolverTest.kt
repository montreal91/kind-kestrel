@file:OptIn(markerClass = [ExperimentalUuidApi::class])

package org.example.rlc.frontend

import co.touchlab.kermit.Logger
import org.example.rlc.application.readFile
import org.example.rlc.frontend.ast.Ast
import org.example.rlc.frontend.ast.FunDeclStmt
import org.example.rlc.frontend.ast.Stmt
import org.example.rlc.frontend.scope.EnclosedVariable
import org.example.rlc.frontend.scope.GlobalVariable
import org.example.rlc.frontend.scope.LocalVariable
import org.example.rlc.frontend.scope.VariableResolutionResult
import org.example.rlc.frontend.scope.VariableResolutionTable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class ResolverTest {
  private val log = this::class.qualifiedName?.let { Logger.withTag(tag = it) }

  @Test
  fun testGlobalAssignAndAccess() {
    val resolutionTable = getResolutionTable(filename = "resolver_01_global.lox")

    val expectedResults = listOf(
      Expectation(fakeUuids[1], GlobalVariable(name = "a")),
      Expectation(fakeUuids[2], GlobalVariable(name = "a")),
    )

    assertResolutions(resolutionTable, expectedResults)
  }

  @Test
  fun testLocalAssignAndAccess() {
    val resolutionTable = getResolutionTable(filename = "resolver_02_local.lox")

    val expectedResults = listOf(
      Expectation(fakeUuids[1], LocalVariable(name = "a", variableArrayIndex = 0)),
      Expectation(fakeUuids[2], LocalVariable(name = "a", variableArrayIndex = 0)),
    )

    assertResolutions(resolutionTable, expectedResults)
  }

  @Test
  fun testShadowing() {
    val resolutionTable = getResolutionTable(filename = "resolver_03_shadowing.lox")

    val expectedResults = listOf(
      Expectation(fakeUuids[1], GlobalVariable(name = "a")),
      Expectation(fakeUuids[3], LocalVariable(name = "a", variableArrayIndex = 0)),
      Expectation(fakeUuids[5], LocalVariable(name = "a",variableArrayIndex = 1)),
      Expectation(fakeUuids[6], LocalVariable(name = "a", variableArrayIndex = 1)),
      Expectation(fakeUuids[9], LocalVariable(name = "a", variableArrayIndex = 0)),
      Expectation(fakeUuids[12], LocalVariable(name = "a", variableArrayIndex = 1)), // Check later if this case can break
      Expectation(fakeUuids[13], LocalVariable(name = "a", variableArrayIndex = 1)), // Check later if this case can break
      Expectation(fakeUuids[16], LocalVariable(name = "a",variableArrayIndex = 0)),
      Expectation(fakeUuids[19], GlobalVariable(name = "a")),
    )

    assertResolutions(resolutionTable, expectedResults)
  }

  @Test
  fun testUpValue01() {
    val resolutionTable = getResolutionTable(filename = "resolver_04_upvalue_01.lox")

    //fun mutate() { global
    //  var cap = "unchanged"; 0
    //
    //  fun inner() { 1
    //    print cap; 0
    //  }
    //
    //  cap = "changed"; 0
    //
    //  return inner; 1
    //}
    //
    //mutate()(); global

    for (k in resolutionTable._test_getKeys()) {
      log?.d(messageString = k.toString())
    }

    val expectedResults = listOf(
      Expectation(fakeUuids[13], GlobalVariable(name = "mutate")),
      Expectation(fakeUuids[1], LocalVariable(name = "cap",variableArrayIndex = 0, isUpValue = true)),
      Expectation(fakeUuids[5], LocalVariable(name = "inner", variableArrayIndex = 1, isUpValue = false)),
      Expectation(
        fakeUuids[2],
        EnclosedVariable(name = "cap", enclosedObject = EnclosedLocal(localVariableIndex = 0), depth = 2)
      ),
      Expectation(fakeUuids[6], LocalVariable(name = "cap", variableArrayIndex = 0, isUpValue = true)),
      Expectation(fakeUuids[10], LocalVariable(name = "inner", variableArrayIndex = 1, isUpValue = false)),
      Expectation(fakeUuids[14], GlobalVariable(name = "mutate")),
    )

    assertResolutions(resolutionTable, expectedResults)
  }

  @Test
  fun testUpvalue02() {
    val resolutionTable = getResolutionTable(filename = "resolver_04_upvalue_02.lox")

    for (k in resolutionTable._test_getKeys()) {
      log?.d(messageString = "$k => ${resolutionTable.get(k)}")
    }

    val expectedResults = listOf(
      Expectation(fakeUuids[26], GlobalVariable(name = "outer")),
      Expectation(fakeUuids[0], LocalVariable(name = "x", variableArrayIndex = 0, isUpValue = true)),
      Expectation(fakeUuids[22], LocalVariable(name = "middle", variableArrayIndex = 1, isUpValue = false)),
      Expectation(fakeUuids[1], LocalVariable(name = "y", variableArrayIndex = 0, isUpValue = true)),
      Expectation(fakeUuids[5], LocalVariable(name = "t", variableArrayIndex = 1, isUpValue = false)),
      Expectation(fakeUuids[2], EnclosedVariable(name = "x", depth = 1, enclosedObject = EnclosedLocal(localVariableIndex = 0))),
      Expectation(fakeUuids[3], LocalVariable(name = "y", variableArrayIndex = 0, isUpValue = false)),
      Expectation(fakeUuids[14], LocalVariable(name = "inner", variableArrayIndex = 2, isUpValue = false)),
      Expectation(fakeUuids[6], LocalVariable(name = "z", variableArrayIndex = 0, isUpValue = false)),
      Expectation(fakeUuids[7], EnclosedVariable(name = "x", depth = 3, enclosedObject = EnclosedUpvalue(variableName = "x"))),
      Expectation(fakeUuids[8], EnclosedVariable(name = "y", depth = 3, enclosedObject = EnclosedLocal(localVariableIndex = 0))),
      Expectation(fakeUuids[10], LocalVariable(name="z", variableArrayIndex = 0, isUpValue = false)),
      Expectation(fakeUuids[17], LocalVariable(name = "t", variableArrayIndex = 1, isUpValue = false)),
      Expectation(fakeUuids[19], LocalVariable(name = "inner", variableArrayIndex = 2, isUpValue = false)),
      Expectation(fakeUuids[23], LocalVariable(name = "middle", variableArrayIndex = 1, isUpValue = false)),
      Expectation(fakeUuids[26], GlobalVariable(name = "outer")),
    )

    assertResolutions(resolutionTable, expectedResults)
  }

  @Test
  fun testUpvalue03() {
    val resolutionContext = getResolutionContext(filename = "resolver_04_upvalue_03.lox")

    for (k in resolutionContext.table._test_getKeys()) {
      println("$k => ${resolutionContext.table.get(k)}")
    }

    println("=============================")
    val astNodeCollector = AstNodeCollector()
    astNodeCollector.collect(resolutionContext.ast)

    val statementMap = astNodeCollector.getStatements()

    val outerFunDeclStmt = statementMap[fakeUuids[18]] as FunDeclStmt

    println("outer enclosed variables")
    for (ev in outerFunDeclStmt.enclosedVariables) {
      println(ev)
    }

    println()
    println("middle enclosed variables")

    val middleFunDeclStmt = statementMap[fakeUuids[14]] as FunDeclStmt

    for (ev in middleFunDeclStmt.enclosedVariables) {
      println(ev)
    }

    println()
    println("inner enclosed variables")

    val innerFunDeclStmt = statementMap[fakeUuids[10]] as FunDeclStmt

    for (ev in innerFunDeclStmt.enclosedVariables) {
      println(ev)
    }

    println("Doin' actual test.")

    val expectedResults = listOf(
      Expectation(fakeUuids[18], GlobalVariable(name = "outer")),
      Expectation(fakeUuids[0], LocalVariable(name = "x", variableArrayIndex = 0, isUpValue = true)),
      Expectation(fakeUuids[14], LocalVariable(name = "middle", variableArrayIndex = 1, isUpValue = false)),
      Expectation(fakeUuids[1], LocalVariable(name = "y", variableArrayIndex = 0, isUpValue = true)),
      Expectation(fakeUuids[10], LocalVariable(name = "inner", variableArrayIndex = 1, isUpValue = false)),
      Expectation(fakeUuids[2], LocalVariable(name = "z", variableArrayIndex = 0, isUpValue = false)),
      Expectation(fakeUuids[3], EnclosedVariable(name = "x", depth = 3, enclosedObject = EnclosedUpvalue(variableName = "x"))),
      Expectation(fakeUuids[4], EnclosedVariable(name = "y", depth = 3, enclosedObject = EnclosedLocal(localVariableIndex = 0))),
      Expectation(fakeUuids[6], LocalVariable(name = "z", variableArrayIndex = 0, isUpValue = false)),
      Expectation(fakeUuids[11], LocalVariable(name = "inner", variableArrayIndex = 1, isUpValue = false)),
      Expectation(fakeUuids[15], LocalVariable(name = "middle", variableArrayIndex = 1, isUpValue = false)),
      Expectation(fakeUuids[24], GlobalVariable(name = "closure")),
      Expectation(fakeUuids[19], GlobalVariable(name = "outer")),
      Expectation(fakeUuids[25], GlobalVariable(name = "closure")),
      Expectation(fakeUuids[32], GlobalVariable(name = "c1")),
      Expectation(fakeUuids[29], GlobalVariable(name = "outer")),
      Expectation(fakeUuids[36], GlobalVariable(name = "c2")),
      Expectation(fakeUuids[33], GlobalVariable(name = "c1")),
      Expectation(fakeUuids[37], GlobalVariable(name = "c2")),
      Expectation(fakeUuids[41], GlobalVariable(name = "outer")),
    )

    assertResolutions(resolutionContext.table, expectedResults)

    val expectedEnclosedMiddle = listOf<VariableResolutionResult>(
      EnclosedVariable(name = "x", depth = -1, enclosedObject = EnclosedLocal(localVariableIndex = 0)),
    )

    val expectedEnclosedInner = listOf<VariableResolutionResult>(
      EnclosedVariable(name = "x", depth = -1, enclosedObject = EnclosedUpvalue(variableName = "x")),
      EnclosedVariable(name = "y", depth = -1, enclosedObject = EnclosedLocal(localVariableIndex = 0)),
    )

    val expectedEnclosedVariables = listOf(
      ExpectedEnclosedVariables(functionDeclarationId = fakeUuids[18], enclosedVariables = emptyList()),
      ExpectedEnclosedVariables(functionDeclarationId = fakeUuids[14], enclosedVariables = expectedEnclosedMiddle),
      ExpectedEnclosedVariables(functionDeclarationId = fakeUuids[10], enclosedVariables = expectedEnclosedInner),
    )

    assertEnclosedVariables(functions = statementMap, expectedEnclosedVariables)
  }

  @Test
  fun testInvalidThis() {
    val text = readFile(pathName = "${PATH_PREFIX}\\resolver_05_invalid_this.lox")
    val scanner = Scanner(text)
    val parser = Parser(tokens = scanner.scan(), uidGen = MockUuidGenerator().mockGen)
    val ast = parser.parse()

    val resolver = Resolver()
    val res = resolver.resolve(program = ast)

    log?.d(messageString = "Resolution errors: ${resolver.hasErrors}")

    assertTrue(actual = resolver.hasErrors)
  }
}

private data class Expectation(val variableId: Uuid, val expectedResult: VariableResolutionResult)

private fun assertResolutions(resolutionTable: VariableResolutionTable, expectedResults: List<Expectation>) {
  for (expected in expectedResults) {
    assertEquals(
      expected = expected.expectedResult,
      actual = resolutionTable.get(expected.variableId),
      message = "For node id ${expected.variableId}"
    )
  }
}

private data class ResolutionContext(val table: VariableResolutionTable, val ast: Ast)

private data class ExpectedEnclosedVariables(
  val functionDeclarationId: Uuid,
  val enclosedVariables: List<VariableResolutionResult>,
)

private fun assertEnclosedVariables(functions: Map<Uuid, Stmt>, expectedResult: List<ExpectedEnclosedVariables>) {
  for (expectation in expectedResult) {
    assertTrue(functions.containsKey(expectation.functionDeclarationId))
    assertTrue(functions[expectation.functionDeclarationId] is FunDeclStmt)
    val function = functions[expectation.functionDeclarationId] as FunDeclStmt
    assertEquals(
      actual = function.enclosedVariables,
      expected = expectation.enclosedVariables,
    )
  }
}

private fun getResolutionContext(filename: String): ResolutionContext {
  val text = readFile(pathName = "${PATH_PREFIX}\\$filename")
  val scanner = Scanner(text)
  val parser = Parser(tokens = scanner.scan(), uidGen = MockUuidGenerator().mockGen)
  val ast = parser.parse()

  val resolver = Resolver()
  return ResolutionContext(table = resolver.resolve(program = ast), ast = ast)
}

private fun getResolutionTable(filename: String): VariableResolutionTable {
  return getResolutionContext(filename).table
}


private val fakeUuids = listOf(
  Uuid.parse(uuidString = "00000000-0000-0000-0000-000000000001"),
  Uuid.parse(uuidString = "00000000-0000-0000-0000-000000000002"),
  Uuid.parse(uuidString = "00000000-0000-0000-0000-000000000003"),
  Uuid.parse(uuidString = "00000000-0000-0000-0000-000000000004"),
  Uuid.parse(uuidString = "00000000-0000-0000-0000-000000000005"),
  Uuid.parse(uuidString = "00000000-0000-0000-0000-000000000006"),
  Uuid.parse(uuidString = "00000000-0000-0000-0000-000000000007"),
  Uuid.parse(uuidString = "00000000-0000-0000-0000-000000000008"),
  Uuid.parse(uuidString = "00000000-0000-0000-0000-000000000009"),
  Uuid.parse(uuidString = "00000000-0000-0000-0000-000000000010"),
  Uuid.parse(uuidString = "00000000-0000-0000-0000-000000000011"),
  Uuid.parse(uuidString = "00000000-0000-0000-0000-000000000012"),
  Uuid.parse(uuidString = "00000000-0000-0000-0000-000000000013"),
  Uuid.parse(uuidString = "00000000-0000-0000-0000-000000000014"),
  Uuid.parse(uuidString = "00000000-0000-0000-0000-000000000015"),
  Uuid.parse(uuidString = "00000000-0000-0000-0000-000000000016"),
  Uuid.parse(uuidString = "00000000-0000-0000-0000-000000000017"),
  Uuid.parse(uuidString = "00000000-0000-0000-0000-000000000018"),
  Uuid.parse(uuidString = "00000000-0000-0000-0000-000000000019"),
  Uuid.parse(uuidString = "00000000-0000-0000-0000-000000000020"),
  Uuid.parse(uuidString = "00000000-0000-0000-0000-000000000021"),
  Uuid.parse(uuidString = "00000000-0000-0000-0000-000000000022"),
  Uuid.parse(uuidString = "00000000-0000-0000-0000-000000000023"),
  Uuid.parse(uuidString = "00000000-0000-0000-0000-000000000024"),
  Uuid.parse(uuidString = "00000000-0000-0000-0000-000000000025"),
  Uuid.parse(uuidString = "00000000-0000-0000-0000-000000000026"),
  Uuid.parse(uuidString = "00000000-0000-0000-0000-000000000027"),
  Uuid.parse(uuidString = "00000000-0000-0000-0000-000000000028"),
  Uuid.parse(uuidString = "00000000-0000-0000-0000-000000000029"),
  Uuid.parse(uuidString = "00000000-0000-0000-0000-000000000030"),
  Uuid.parse(uuidString = "00000000-0000-0000-0000-000000000031"),
  Uuid.parse(uuidString = "00000000-0000-0000-0000-000000000032"),
  Uuid.parse(uuidString = "00000000-0000-0000-0000-000000000033"),
  Uuid.parse(uuidString = "00000000-0000-0000-0000-000000000034"),
  Uuid.parse(uuidString = "00000000-0000-0000-0000-000000000035"),
  Uuid.parse(uuidString = "00000000-0000-0000-0000-000000000036"),
  Uuid.parse(uuidString = "00000000-0000-0000-0000-000000000037"),
  Uuid.parse(uuidString = "00000000-0000-0000-0000-000000000038"),
  Uuid.parse(uuidString = "00000000-0000-0000-0000-000000000039"),
  Uuid.parse(uuidString = "00000000-0000-0000-0000-000000000040"),
  Uuid.parse(uuidString = "00000000-0000-0000-0000-000000000041"),
  Uuid.parse(uuidString = "00000000-0000-0000-0000-000000000042"),
  Uuid.parse(uuidString = "00000000-0000-0000-0000-000000000043"),
  Uuid.parse(uuidString = "00000000-0000-0000-0000-000000000044"),
  Uuid.parse(uuidString = "00000000-0000-0000-0000-000000000045"),
  Uuid.parse(uuidString = "00000000-0000-0000-0000-000000000046"),
  Uuid.parse(uuidString = "00000000-0000-0000-0000-000000000047"),
  Uuid.parse(uuidString = "00000000-0000-0000-0000-000000000048"),
  Uuid.parse(uuidString = "00000000-0000-0000-0000-000000000049"),
  Uuid.parse(uuidString = "00000000-0000-0000-0000-000000000050"),
)

private class MockUuidGenerator {
  private var index = 0
  val mockGen: () -> Uuid = { fakeUuids[index++] }
}
