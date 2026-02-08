package space.be1ski.vibits.core.utils.coroutines

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class RunSuspendCatchingTest {
  @Test
  fun `when runSuspendCatching with successful block then returns success`() =
    runTest {
      val result = runSuspendCatching { 42 }

      assertTrue(result.isSuccess)
      assertEquals(42, result.getOrNull())
    }

  @Test
  fun `when runSuspendCatching with exception then returns failure`() =
    runTest {
      val expected = IllegalStateException("boom")

      val result = runSuspendCatching { throw expected }

      assertTrue(result.isFailure)
      assertEquals(expected, result.exceptionOrNull())
    }

  @Test
  fun `when runSuspendCatching with CancellationException then rethrows`() =
    runTest {
      assertFailsWith<CancellationException> {
        runSuspendCatching { throw CancellationException("cancelled") }
      }
    }

  @Test
  fun `when extension runSuspendCatching with successful block then returns success`() =
    runTest {
      val result = "hello".runSuspendCatching { uppercase() }

      assertTrue(result.isSuccess)
      assertEquals("HELLO", result.getOrNull())
    }

  @Test
  fun `when extension runSuspendCatching with exception then returns failure`() =
    runTest {
      val expected = RuntimeException("fail")

      val result = "hello".runSuspendCatching { throw expected }

      assertTrue(result.isFailure)
      assertEquals(expected, result.exceptionOrNull())
    }

  @Test
  fun `when extension runSuspendCatching with CancellationException then rethrows`() =
    runTest {
      assertFailsWith<CancellationException> {
        "hello".runSuspendCatching { throw CancellationException("cancelled") }
      }
    }
}
