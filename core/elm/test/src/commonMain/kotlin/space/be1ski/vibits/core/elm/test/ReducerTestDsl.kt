package space.be1ski.vibits.core.elm.test

import space.be1ski.vibits.core.elm.Reducer
import space.be1ski.vibits.core.elm.ReducerResult
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * DSL for testing reducers with Command/Notification separation.
 *
 * Example:
 * ```
 * @Test
 * fun `increment increases counter`() = counterReducer.test(CounterState(count = 5)) {
 *   send(CounterAction.Increment)
 *
 *   assertState { count == 6 }
 *   assertNoCommands()
 *   assertNoNotifications()
 * }
 *
 * @Test
 * fun `reset clears counter and emits command and notification`() = counterReducer.test(CounterState(count = 100)) {
 *   send(CounterAction.Reset)
 *
 *   assertState(CounterState(count = 0))
 *   assertCommands(CounterCommand.Persist)
 *   assertNotifications(CounterNotification.ResetCompleted)
 * }
 * ```
 */
fun <Action, State, Command, Notification> Reducer<Action, State, Command, Notification>.test(
  initialState: State,
  block: ReducerTestScope<Action, State, Command, Notification>.() -> Unit,
) {
  ReducerTestScope(this, initialState).apply(block)
}

@Suppress("TooManyFunctions") // Test DSL requires many helper methods
class ReducerTestScope<Action, State, Command, Notification>(
  private val reducer: Reducer<Action, State, Command, Notification>,
  initialState: State,
) {
  private var currentState: State = initialState
  private var lastResult: ReducerResult<State, Command, Notification>? = null
  private val allCommands = mutableListOf<Command>()

  /**
   * Sends an action to the reducer and captures the result.
   */
  fun send(action: Action) {
    lastResult = reducer(action, currentState)
    currentState = lastResult!!.state
    allCommands.addAll(lastResult!!.commands)
  }

  /**
   * Returns the current state after all sent actions.
   */
  val state: State get() = currentState

  /**
   * Returns commands from the last action only.
   */
  val commands: List<Command> get() = lastResult?.commands ?: emptyList()

  /**
   * Returns notifications from the last action only.
   */
  val notifications: List<Notification> get() = lastResult?.notifications ?: emptyList()

  /**
   * Returns all commands accumulated from all sent actions.
   */
  val allEmittedCommands: List<Command> get() = allCommands.toList()

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
   * Asserts that no commands or notifications were emitted from the last action.
   */
  fun assertNoEffects() {
    assertNoCommands()
    assertNoNotifications()
  }

  /**
   * Asserts that no commands were emitted from the last action.
   */
  fun assertNoCommands() {
    assertTrue(commands.isEmpty(), "Expected no commands but got: $commands")
  }

  /**
   * Asserts that no notifications were emitted from the last action.
   */
  fun assertNoNotifications() {
    assertTrue(notifications.isEmpty(), "Expected no notifications but got: $notifications")
  }

  /**
   * Asserts that the last action emitted exactly these commands in order.
   */
  fun assertCommands(vararg expected: Command) {
    assertEquals(expected.toList(), commands, "Commands mismatch")
  }

  /**
   * Asserts that the last action emitted exactly these notifications in order.
   */
  fun assertNotifications(vararg expected: Notification) {
    assertEquals(expected.toList(), notifications, "Notifications mismatch")
  }

  /**
   * Asserts that the last action emitted a command of the given type.
   */
  inline fun <reified C : Command> assertHasCommand(): C {
    val command =
      commands.filterIsInstance<C>().firstOrNull()
        ?: fail("Expected command of type ${C::class.simpleName} but got: $commands")
    return command
  }

  /**
   * Asserts that the last action emitted a command matching the predicate.
   */
  inline fun <reified C : Command> assertHasCommand(predicate: (C) -> Boolean): C {
    val command =
      commands.filterIsInstance<C>().firstOrNull(predicate)
        ?: fail("Expected command of type ${C::class.simpleName} matching predicate but got: $commands")
    return command
  }

  /**
   * Asserts that the last action emitted a notification of the given type.
   */
  inline fun <reified N : Notification> assertHasNotification(): N {
    val notification =
      notifications.filterIsInstance<N>().firstOrNull()
        ?: fail("Expected notification of type ${N::class.simpleName} but got: $notifications")
    return notification
  }

  /**
   * Asserts the number of commands emitted from the last action.
   */
  fun assertCommandCount(expected: Int) {
    assertEquals(expected, commands.size, "Command count mismatch")
  }
}
