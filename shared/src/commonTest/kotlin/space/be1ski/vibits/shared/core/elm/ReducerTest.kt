package space.be1ski.vibits.shared.core.elm

import kotlin.test.Test
import kotlin.test.assertEquals

class ReducerTest {
  private data class CounterState(
    val count: Int = 0,
    val lastAction: String = "",
  )

  private sealed interface CounterAction {
    data object Increment : CounterAction

    data object Decrement : CounterAction

    data class Add(
      val amount: Int,
    ) : CounterAction

    data object Reset : CounterAction

    data object Save : CounterAction
  }

  private sealed interface CounterEffect {
    data class Log(
      val message: String,
    ) : CounterEffect

    data object Persist : CounterEffect

    data object NotifyReset : CounterEffect
  }

  private val counterReducer: Reducer<CounterAction, CounterState, CounterEffect> =
    reducer { action, state ->
      when (action) {
        CounterAction.Increment -> state { copy(count = count + 1, lastAction = "increment") }
        CounterAction.Decrement -> state { copy(count = count - 1, lastAction = "decrement") }
        is CounterAction.Add -> {
          state { copy(count = count + action.amount, lastAction = "add") }
          effect(CounterEffect.Log("Added ${action.amount}"))
        }
        CounterAction.Reset -> {
          state { copy(count = 0, lastAction = "reset") }
          effects(CounterEffect.Persist, CounterEffect.NotifyReset)
        }
        CounterAction.Save -> effect(CounterEffect.Persist)
      }
    }

  @Test
  fun `when state-only action then state updates and no effects`() {
    val result = counterReducer(CounterAction.Increment, CounterState(count = 5))

    assertEquals(6, result.state.count)
    assertEquals("increment", result.state.lastAction)
    assertEquals(emptyList(), result.effects)
  }

  @Test
  fun `when action with state and effect then both are returned`() {
    val result = counterReducer(CounterAction.Add(10), CounterState(count = 5))

    assertEquals(15, result.state.count)
    assertEquals("add", result.state.lastAction)
    assertEquals(listOf(CounterEffect.Log("Added 10")), result.effects)
  }

  @Test
  fun `when action with multiple effects then all effects are returned`() {
    val result = counterReducer(CounterAction.Reset, CounterState(count = 100))

    assertEquals(0, result.state.count)
    assertEquals("reset", result.state.lastAction)
    assertEquals(listOf(CounterEffect.Persist, CounterEffect.NotifyReset), result.effects)
  }

  @Test
  fun `when effect-only action then state is preserved`() {
    val initialState = CounterState(count = 42, lastAction = "previous")

    val result = counterReducer(CounterAction.Save, initialState)

    assertEquals(initialState, result.state)
    assertEquals(listOf(CounterEffect.Persist), result.effects)
  }

  @Test
  fun `when same input then same output (purity)`() {
    val state = CounterState(count = 5)
    val action = CounterAction.Add(3)

    val result1 = counterReducer(action, state)
    val result2 = counterReducer(action, state)

    assertEquals(result1, result2)
  }

  @Test
  fun `when chaining actions then final state is correct`() {
    var state = CounterState()

    state = counterReducer(CounterAction.Increment, state).state
    state = counterReducer(CounterAction.Increment, state).state
    state = counterReducer(CounterAction.Add(5), state).state
    state = counterReducer(CounterAction.Decrement, state).state

    assertEquals(6, state.count)
  }
}
