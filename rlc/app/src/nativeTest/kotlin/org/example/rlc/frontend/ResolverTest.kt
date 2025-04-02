@file:OptIn(ExperimentalUuidApi::class, ExperimentalUuidApi::class)

package org.example.rlc.frontend

import org.example.rlc.application.readFile
import org.example.rlc.frontend.scope.EnclosedVariable
import org.example.rlc.frontend.scope.GlobalVariable
import org.example.rlc.frontend.scope.LocalVariable
import org.example.rlc.frontend.scope.VariableResolutionResult
import org.example.rlc.frontend.scope.VariableResolutionTable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

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
)

private class MockUuidGenerator {
  private var index = 0
  val mockGen: () -> Uuid = { fakeUuids[index++] }
}

@OptIn(ExperimentalUuidApi::class)
class ResolverTest {
  @Test
  fun testGlobalAssignAndAccess() {
//    index = 0
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
      Expectation(fakeUuids[1], LocalVariable(variableArrayIndex = 0)),
      Expectation(fakeUuids[2], LocalVariable(variableArrayIndex = 0)),
    )

    assertResolutions(resolutionTable, expectedResults)
  }

  @Test
  fun testShadowing() {
    val resolutionTable = getResolutionTable(filename = "resolver_03_shadowing.lox")

    val expectedResults = listOf(
      Expectation(fakeUuids[1], GlobalVariable(name = "a")),
      Expectation(fakeUuids[3], LocalVariable(variableArrayIndex = 0)),
      Expectation(fakeUuids[5], LocalVariable(variableArrayIndex = 1)),
      Expectation(fakeUuids[6], LocalVariable(variableArrayIndex = 1)),
      Expectation(fakeUuids[9], LocalVariable(variableArrayIndex = 0)),
      Expectation(fakeUuids[12], LocalVariable(variableArrayIndex = 1)), // Check later if this case can break
      Expectation(fakeUuids[13], LocalVariable(variableArrayIndex = 1)), // Check later if this case can break
      Expectation(fakeUuids[16], LocalVariable(variableArrayIndex = 0)),
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
      println(k)
    }

    val expectedResults = listOf(
      Expectation(fakeUuids[13], GlobalVariable(name = "mutate")),
      Expectation(fakeUuids[1], LocalVariable(variableArrayIndex = 0, isUpValue = true)),
      Expectation(fakeUuids[5], LocalVariable(variableArrayIndex = 1, isUpValue = false)),
      Expectation(
        fakeUuids[2],
        EnclosedVariable(name = "cap", enclosedObject = EnclosedLocal(localVariableIndex = 0), depth = 2)
      ),
      Expectation(fakeUuids[6], LocalVariable(variableArrayIndex = 0, isUpValue = true)),
      Expectation(fakeUuids[10], LocalVariable(variableArrayIndex = 1, isUpValue = false)),
      Expectation(fakeUuids[14], GlobalVariable(name = "mutate")),
    )

    assertResolutions(resolutionTable, expectedResults)
  }
}

private data class Expectation(val variableId: Uuid, val expectedResult: VariableResolutionResult)

private fun assertResolutions(resolutionTable: VariableResolutionTable, expectedResults: List<Expectation>) {
  for (expected in expectedResults) {
    assertEquals(expected = expected.expectedResult, actual = resolutionTable.get(expected.variableId))
  }
}

private fun getResolutionTable(filename: String): VariableResolutionTable {
  val text = readFile(pathName = "${PATH_PREFIX}\\$filename")
  val scanner = Scanner(text)
  val parser = Parser(tokens = scanner.scan(), uidGen = MockUuidGenerator().mockGen)
  val ast = parser.parse()

  val resolver = Resolver()
  return resolver.resolve(ast)
}
