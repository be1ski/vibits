package space.be1ski.vibits.core.elm

import space.be1ski.vibits.core.elm.test.test
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ReducerTestDslTest {
  private data class State(
    val count: Int = 0,
    val text: String = "",
  )

  private sealed interface Action {
    data object Increment : Action

    data class SetText(
      val value: String,
    ) : Action

    data object Save : Action

    data object Complex : Action
  }

  private sealed interface Effect {
    data object Persist : Effect

    data class Log(
      val msg: String,
    ) : Effect

    data object Notify : Effect
  }

  private val testReducer: Reducer<Action, State, Effect, Nothing> =
    reducer { action, state ->
      when (action) {
        Action.Increment -> state { state.copy(count = state.count + 1) }
        is Action.SetText -> state { state.copy(text = action.value) }
        Action.Save -> command(Effect.Persist)
        Action.Complex -> {
          state { state.copy(count = state.count + 10, text = "complex") }
          commands(Effect.Persist, Effect.Log("complex action"), Effect.Notify)
        }
      }
    }

  @Test
  fun `when simple state change then state is updated`() =
    testReducer.test(State(count = 5)) {
      send(Action.Increment)

      assertState(State(count = 6))
      assertNoEffects()
    }

  @Test
  fun `when using state predicate then assertion works`() =
    testReducer.test(State()) {
      send(Action.Increment)
      send(Action.Increment)
      send(Action.Increment)

      assertState { count == 3 }
    }

  @Test
  fun `when command-only action then state is preserved`() =
    testReducer.test(State(count = 42, text = "preserved")) {
      send(Action.Save)

      assertState(State(count = 42, text = "preserved"))
      assertCommands(Effect.Persist)
    }

  @Test
  fun `when action with multiple commands then all commands are captured`() =
    testReducer.test(State()) {
      send(Action.Complex)

      assertCommands(Effect.Persist, Effect.Log("complex action"), Effect.Notify)
      assertCommandCount(3)
    }

  @Test
  fun `when assertHasCommand then command is found by type`() =
    testReducer.test(State()) {
      send(Action.Complex)

      assertHasCommand<Effect.Persist>()
      assertHasCommand<Effect.Notify>()
      val log = assertHasCommand<Effect.Log>()
      assertEquals("complex action", log.msg)
    }

  @Test
  fun `when assertHasCommand with predicate then matching command is found`() =
    testReducer.test(State()) {
      send(Action.Complex)

      assertHasCommand<Effect.Log> { it.msg.contains("complex") }
    }

  @Test
  fun `when chaining actions then state accumulates correctly`() =
    testReducer.test(State()) {
      send(Action.Increment)
      assertState { count == 1 }
      assertNoEffects()

      send(Action.SetText("hello"))
      assertState { text == "hello" && count == 1 }

      send(Action.Increment)
      assertState(State(count = 2, text = "hello"))
    }

  @Test
  fun `when multiple actions then allEmittedCommands accumulates`() =
    testReducer.test(State()) {
      send(Action.Save)
      send(Action.Complex)

      assertCommandCount(3) // Last action only
      assertEquals(4, allEmittedCommands.size) // All commands
    }

  @Test
  fun `when accessing state property then current state is returned`() =
    testReducer.test(State(count = 10)) {
      assertEquals(10, state.count)

      send(Action.Increment)
      assertEquals(11, state.count)
    }

  @Test
  fun `when assertState fails then AssertionError is thrown`() {
    assertFailsWith<AssertionError> {
      testReducer.test(State()) {
        send(Action.Increment)
        assertState(State(count = 999))
      }
    }
  }

  @Test
  fun `when assertNoEffects with commands then AssertionError is thrown`() {
    assertFailsWith<AssertionError> {
      testReducer.test(State()) {
        send(Action.Save)
        assertNoEffects()
      }
    }
  }
}
