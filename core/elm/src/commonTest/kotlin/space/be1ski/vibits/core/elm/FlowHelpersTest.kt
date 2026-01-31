package space.be1ski.vibits.core.elm

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FlowHelpersTest {
  private sealed interface TestAction : Action {
    data class Value(
      val value: String,
    ) : TestAction
  }

  @Test
  fun `when action with value then emits single value`() =
    runTest {
      val testValue = TestAction.Value("test")
      val result = action(testValue).toList()
      assertEquals(listOf(testValue), result)
    }

  @Test
  fun `when actions with multiple emissions then emits all values`() =
    runTest {
      val result =
        actions<TestAction> {
          emit(TestAction.Value("first"))
          emit(TestAction.Value("second"))
          emit(TestAction.Value("third"))
        }.toList()
      assertEquals(
        listOf(
          TestAction.Value("first"),
          TestAction.Value("second"),
          TestAction.Value("third"),
        ),
        result,
      )
    }

  @Test
  fun `when actions with empty block then emits nothing`() =
    runTest {
      val result =
        actions<TestAction> {
          // Empty block
        }.toList()
      assertTrue(result.isEmpty())
    }

  @Test
  fun `when sideEffect with block then executes block but emits nothing`() =
    runTest {
      var executed = false
      val result =
        sideEffect<TestAction> {
          executed = true
        }.toList()
      assertTrue(executed)
      assertTrue(result.isEmpty())
    }

  @Test
  fun `when noActions then returns empty flow`() =
    runTest {
      val result = noActions<TestAction>().toList()
      assertTrue(result.isEmpty())
    }
}
