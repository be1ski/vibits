@file:Suppress("RedundantVisibilityModifier")

package space.be1ski.vibits.shared.core.elm

/**
 * Creates a [Reducer] using the DSL syntax with Command/Notification separation.
 *
 * Simple example:
 * ```
 * val myReducer = reducer<MyAction, MyState, MyCommand, MyNotification> { action, state ->
 *   when (action) {
 *     is MyAction.IncrementCounter -> state { copy(counter = counter++) }
 *     is MyAction.Reset -> {
 *       state { copy(counter = 0) }
 *       notify(MyNotification.ResetCompleted)
 *     }
 *     is MyAction.LoadData -> command(MyCommand.FetchData)
 *     is MyAction.Multiple -> {
 *       commands(MyCommand.FetchData, MyCommand.ShowLoading)
 *       notify(MyNotification.LoadingStarted)
 *     }
 *   }
 * }
 * ```
 *
 * For features with only commands and no notifications, use [Nothing] as Notification type:
 * ```
 * val myReducer = reducer<MyAction, MyState, MyCommand, Nothing> { action, state ->
 *   when (action) {
 *     is MyAction.LoadData -> command(MyCommand.FetchData)
 *     // notify(...) cannot be called - compile error
 *   }
 * }
 * ```
 *
 * Example when reducer is extracted to a different file:
 * ```
 * class MyReducer : Reducer<MyAction, MyState, MyCommand, MyNotification> by reducer({ action, state ->
 *   when (action) {
 *     is MyAction.Reset -> handleReset(state)
 *     ...
 *   }
 * })
 *
 * fun ReducerContext<MyState, MyCommand, MyNotification>.handleReset(state: MyState) {
 *   state { copy(counter = 0) }
 *   notify(MyNotification.ResetCompleted)
 * }
 * ```
 */
public fun <Action, State, Command, Notification> reducer(
  reduce: ReducerContext<State, Command, Notification>.(Action, State) -> Unit,
): Reducer<Action, State, Command, Notification> =
  { action, state ->
    ReducerContext<State, Command, Notification>().apply { reduce(action, state) }.getResult(state)
  }

/**
 * Builder context for creating [ReducerResult] instances in a DSL-style with Command/Notification separation.
 *
 * This class is used within the [reducer] function to provide a convenient way to build
 * state updates and collect commands and notifications. It supports multiple patterns:
 *
 * - State update only: `state { copy(count = count + 1) }`
 * - Command only: `command(MyCommand.FetchData)`
 * - Notification only: `notify(MyNotification.Completed)`
 * - State and commands/notifications: combine in any order
 * - Multiple commands: `commands(Command.A, Command.B)` or `commands(listOf(...))`
 * - Multiple notifications: `notifications(Notification.A, Notification.B)`
 *
 * Note: If [state] is called multiple times, only the last transformation is applied.
 * Commands and notifications accumulate with each call.
 */
public class ReducerContext<State, Command, Notification> {
  private var stateUpdate: (State.() -> State) = { this }
  private val commands = mutableListOf<Command>()
  private val notifications = mutableListOf<Notification>()

  /**
   * Updates the state using a transformation lambda.
   *
   * The lambda receives the current state as `this`, allowing direct property access:
   * ```
   * state { copy(count = count + 1, name = "updated") }
   * ```
   *
   * @param update A transformation function that produces the new state
   */
  public fun state(update: State.() -> State) {
    stateUpdate = update
  }

  /**
   * Replaces the state with a new value directly.
   *
   * ```
   * state(MyState(count = 0, name = "reset"))
   * ```
   *
   * @param newState The new state to set
   */
  public fun state(newState: State) {
    state { newState }
  }

  /**
   * Adds a single command to be executed by the EffectHandler.
   *
   * Commands are internal side effects that are processed by the EffectHandler
   * and never exposed to external observers.
   *
   * ```
   * command(MyCommand.FetchData)
   * ```
   *
   * @param command The command to add
   */
  public fun command(command: Command) {
    commands.add(command)
  }

  /**
   * Adds multiple commands to be executed by the EffectHandler.
   *
   * ```
   * commands(MyCommand.SaveData, MyCommand.ShowLoading, MyCommand.LogAnalytics)
   * ```
   *
   * @param commands The commands to add
   */
  public fun commands(vararg commands: Command) {
    commands(commands.toList())
  }

  /**
   * Adds a list of commands to be executed by the EffectHandler.
   *
   * ```
   * commands(listOf(MyCommand.SaveData, MyCommand.ShowLoading))
   * ```
   *
   * @param commands The list of commands to add
   */
  public fun commands(commands: List<Command>) {
    this.commands.addAll(commands)
  }

  /**
   * Emits a single notification to external observers (coordinators, UI).
   *
   * Notifications are external signals that are exposed to coordinators and UI
   * and never reach the EffectHandler.
   *
   * ```
   * notify(MyNotification.Completed)
   * ```
   *
   * Note: For features with Notification type = [Nothing], calling this method
   * will result in a compile error, which is the desired behavior.
   *
   * @param notification The notification to emit
   */
  public fun notify(notification: Notification) {
    notifications.add(notification)
  }

  /**
   * Emits multiple notifications to external observers.
   *
   * ```
   * notifications(MyNotification.Saved, MyNotification.DialogClosed)
   * ```
   *
   * @param notifications The notifications to emit
   */
  public fun notifications(vararg notifications: Notification) {
    notifications(notifications.toList())
  }

  /**
   * Emits a list of notifications to external observers.
   *
   * ```
   * notifications(listOf(MyNotification.Saved, MyNotification.DialogClosed))
   * ```
   *
   * @param notifications The list of notifications to emit
   */
  public fun notifications(notifications: List<Notification>) {
    this.notifications.addAll(notifications)
  }

  internal fun getResult(initialState: State): ReducerResult<State, Command, Notification> =
    ReducerResult(
      state = stateUpdate(initialState),
      effects = Effects(commands = commands, notifications = notifications),
    )
}
