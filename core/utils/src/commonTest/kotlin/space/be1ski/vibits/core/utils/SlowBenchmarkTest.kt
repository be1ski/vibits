package space.be1ski.vibits.core.utils

import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

class SlowBenchmarkTest {
  @Test
  fun `slow test to verify benchmark tracking`() =
    runTest {
      delay(10_000)
      assertTrue(true)
    }
}
