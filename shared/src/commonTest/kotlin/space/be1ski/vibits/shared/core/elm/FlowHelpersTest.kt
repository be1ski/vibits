package space.be1ski.vibits.shared.core.elm

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FlowHelpersTest {
  @Test
  fun `action emits single value`() =
    runTest {
      val result = action("test").toList()
      assertEquals(listOf("test"), result)
    }

  @Test
  fun `actions emits multiple values`() =
    runTest {
      val result =
        actions<String> {
          emit("first")
          emit("second")
          emit("third")
        }.toList()
      assertEquals(listOf("first", "second", "third"), result)
    }

  @Test
  fun `actions emits no values when empty block`() =
    runTest {
      val result =
        actions<String> {
          // Empty block
        }.toList()
      assertTrue(result.isEmpty())
    }

  @Test
  fun `sideEffect executes block but emits nothing`() =
    runTest {
      var executed = false
      val result =
        sideEffect<String> {
          executed = true
        }.toList()
      assertTrue(executed)
      assertTrue(result.isEmpty())
    }

  @Test
  fun `noActions returns empty flow`() =
    runTest {
      val result = noActions<String>().toList()
      assertTrue(result.isEmpty())
    }
}
