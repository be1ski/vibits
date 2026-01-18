package space.be1ski.vibits.shared.core.elm

import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * DSL for testing reducers in a readable and concise way.
 *
 * Example:
 * ```
 * @Test
 * fun `increment increases counter`() = counterReducer.test(CounterState(count = 5)) {
 *   send(CounterAction.Increment)
 *
 *   assertState { count == 6 }
 *   assertNoEffects()
 * }
 *
 * @Test
 * fun `reset clears counter and emits effects`() = counterReducer.test(CounterState(count = 100)) {
 *   send(CounterAction.Reset)
 *
 *   assertState(CounterState(count = 0))
 *   assertEffects(CounterEffect.Persist, CounterEffect.NotifyReset)
 * }
 * ```
 */
fun <Action, State, Effect> Reducer<Action, State, Effect>.test(
  initialState: State,
  block: ReducerTestScope<Action, State, Effect>.() -> Unit,
) {
  ReducerTestScope(this, initialState).apply(block)
}

class ReducerTestScope<Action, State, Effect>(
  private val reducer: Reducer<Action, State, Effect>,
  initialState: State,
) {
  private var currentState: State = initialState
  private var lastResult: ReducerResult<State, Effect>? = null
  private val allEffects = mutableListOf<Effect>()

  /**
   * Sends an action to the reducer and captures the result.
   */
  fun send(action: Action) {
    lastResult = reducer(action, currentState)
    currentState = lastResult!!.state
    allEffects.addAll(lastResult!!.effects)
  }

  /**
   * Returns the current state after all sent actions.
   */
  val state: State get() = currentState

  /**
   * Returns effects from the last action only.
   */
  val effects: List<Effect> get() = lastResult?.effects ?: emptyList()

  /**
   * Returns all effects accumulated from all sent actions.
   */
  val allEmittedEffects: List<Effect> get() = allEffects.toList()

  /**
   * Asserts that the current state equals the expected state.
   */
  fun assertState(expected: State) {
    assertEquals(expected, currentState, "State mismatch")
  }

  /**
   * Asserts that the current state satisfies the given predicate.
   */
  fun assertState(
    message: String? = null,
    predicate: State.() -> Boolean,
  ) {
    assertTrue(
      predicate(currentState),
      message ?: "State assertion failed. Current state: $currentState",
    )
  }

  /**
   * Asserts that no effects were emitted from the last action.
   */
  fun assertNoEffects() {
    assertTrue(effects.isEmpty(), "Expected no effects but got: $effects")
  }

  /**
   * Asserts that the last action emitted exactly these effects in order.
   */
  fun assertEffects(vararg expected: Effect) {
    assertEquals(expected.toList(), effects, "Effects mismatch")
  }

  /**
   * Asserts that the last action emitted exactly these effects in order.
   */
  fun assertEffects(expected: List<Effect>) {
    assertEquals(expected, effects, "Effects mismatch")
  }

  /**
   * Asserts that the last action emitted an effect of the given type.
   */
  inline fun <reified E : Effect> assertHasEffect(): E {
    val effect =
      effects.filterIsInstance<E>().firstOrNull()
        ?: fail("Expected effect of type ${E::class.simpleName} but got: $effects")
    return effect
  }

  /**
   * Asserts that the last action emitted an effect matching the predicate.
   */
  inline fun <reified E : Effect> assertHasEffect(predicate: (E) -> Boolean): E {
    val effect =
      effects.filterIsInstance<E>().firstOrNull(predicate)
        ?: fail("Expected effect of type ${E::class.simpleName} matching predicate but got: $effects")
    return effect
  }

  /**
   * Asserts the number of effects emitted from the last action.
   */
  fun assertEffectCount(expected: Int) {
    assertEquals(expected, effects.size, "Effect count mismatch")
  }
}
