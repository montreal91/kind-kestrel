
package org.example

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class AppTest {
  @Test
  fun appHasAGreeting() {
    val classUnderTest = App()
    assertNotNull(classUnderTest.greeting, "app should have a greeting")
  }

  @Test
  fun testWhen() {
    assertEquals(-1, complexWhen(1))
    assertEquals(2, complexWhen(2))
    assertEquals(3, complexWhen(3))
    assertEquals(2, complexWhen(4))
    assertEquals(5, complexWhen(5))
    assertEquals(2, complexWhen(6))
    assertEquals(-1, complexWhen(7))
    assertEquals(2, complexWhen(8))
    assertEquals(3, complexWhen(9))
    assertEquals(2, complexWhen(10))
    assertEquals(-1, complexWhen(11))
    assertEquals(2, complexWhen(12))
    assertEquals(-1, complexWhen(13))
    assertEquals(2, complexWhen(14))
    assertEquals(3, complexWhen(15))
    assertEquals(2, complexWhen(16))
    assertEquals(-1, complexWhen(17))
    assertEquals(2, complexWhen(18))
    assertEquals(-1, complexWhen(19))
    assertEquals(2, complexWhen(20))
  }

  private fun complexWhen(value: Int) = when {
    (value % 2 == 0) -> 2
    (value % 3 == 0) -> 3
    (value % 5 == 0) -> 5
    else -> -1
  }
}
