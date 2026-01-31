@file:Suppress("RedundantVisibilityModifier")

package space.be1ski.vibits.core.elm

/**
 * DslMarker to prevent scope pollution in reducer DSL.
 * Prevents calling command()/notify() inside state {} block.
 */
@DslMarker
public annotation class ReducerDslMarker

/**
 * Scope for state update lambdas that prevents calling [ReducerContext.command] or
 * [ReducerContext.notify] inside [ReducerContext.state] block.
 *
 * Access state via the `state` parameter from outer reducer scope:
 * ```
 * reducer { action, state ->
 *   state { state.copy(counter = state.counter + 1) }
 * }
 * ```
 */
@ReducerDslMarker
public object StateUpdateScope

/**
 * Creates a [Reducer] using the DSL syntax with Command/Notification separation.
 *
 * Example:
 * ```
 * val myReducer = reducer<MyAction, MyState, MyCommand, MyNotification> { action, state ->
 *   when (action) {
 *     is MyAction.IncrementCounter -> state { state.copy(counter = state.counter + 1) }
 *     is MyAction.Reset -> {
 *       state { state.copy(counter = 0) }
 *       notify(MyNotification.ResetCompleted)
 *     }
 *     is MyAction.LoadData -> command(MyCommand.FetchData)
 *   }
 * }
 * ```
 *
 * Inside [state] block, [command] and [notify] are NOT accessible due to [ReducerDslMarker].
 */
public fun <Action, State, Command, Notification> reducer(
  reduce: ReducerContext<State, Command, Notification>.(Action, State) -> Unit,
): Reducer<Action, State, Command, Notification> =
  { action, state ->
    ReducerContext<State, Command, Notification>().apply { reduce(action, state) }.getResult(state)
  }

/**
 * Builder context for creating [ReducerResult] instances in a DSL-style.
 *
 * If [state] is called multiple times, only the last transformation is applied.
 * Commands and notifications accumulate with each call.
 */
@ReducerDslMarker
public class ReducerContext<State, Command, Notification> {
  private var stateUpdate: (State.() -> State) = { this }
  private val commands = mutableListOf<Command>()
  private val notifications = mutableListOf<Notification>()

  /**
   * Updates the state using a transformation lambda.
   *
   * Access state via the `state` parameter from outer reducer scope:
   * ```
   * reducer { action, state ->
   *   state { state.copy(count = state.count + 1) }
   * }
   * ```
   *
   * Due to [ReducerDslMarker], [command] and [notify] are NOT accessible inside this block.
   *
   * @param update A transformation function that produces the new state
   */
  public fun state(update: StateUpdateScope.() -> State) {
    stateUpdate = { StateUpdateScope.update() }
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
    stateUpdate = { newState }
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
