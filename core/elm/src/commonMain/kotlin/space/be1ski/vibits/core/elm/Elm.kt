@file:Suppress("RedundantVisibilityModifier")

package space.be1ski.vibits.core.elm

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * A Feature represents a self-contained unit of application logic following The Elm Architecture (TEA).
 *
 * The Elm Architecture is a unidirectional data flow pattern where:
 * - [State] represents the current state of the feature
 * - [Action] represents events that can modify the state
 * - [Command] represents internal side effects handled by the EffectHandler
 * - [Notification] represents external signals for coordinators and UI
 *
 * Data flows in one direction:
 * ```
 * Action -> Reducer -> (State, Commands, Notifications)
 *                          |           |
 *                          v           v
 *                   EffectHandler  Coordinators/UI
 *                          |
 *                          v
 *                       Action
 * ```
 *
 * Commands are processed internally by the EffectHandler and never exposed externally.
 * Notifications are exposed to coordinators/UI and never reach the EffectHandler.
 *
 * Example usage:
 * ```
 * val feature = FeatureImpl(
 *   initialState = CounterState(count = 0),
 *   reducer = counterReducer,
 *   effectHandler = counterEffectHandler,
 * )
 *
 * // Start processing actions and commands
 * feature.launchIn(viewModelScope)
 *
 * // Observe state
 * feature.state.collect { state -> updateUI(state) }
 *
 * // Observe notifications (external events only)
 * feature.notifications.collect { notification -> handleNotification(notification) }
 *
 * // Send actions
 * feature.send(CounterAction.Increment)
 * ```
 *
 * For features with only commands and no notifications, use [Nothing] as the Notification type:
 * ```
 * Feature<MyAction, MyState, MyCommand, Nothing>
 * ```
 *
 * @param Action The type of actions this feature accepts
 * @param State The type of state this feature manages
 * @param Command The type of internal commands processed by EffectHandler
 * @param Notification The type of external notifications for coordinators/UI
 */
public interface Feature<Action, State, Command, Notification> {
  /**
   * The current state of the feature as a [StateFlow].
   *
   * Observers can collect this flow to receive state updates.
   * The flow always has a current value (hot flow).
   */
  public val state: StateFlow<State>

  /**
   * A [Flow] of notifications produced by the feature.
   *
   * Notifications are emitted when the reducer returns them. External observers (coordinators,
   * UI, analytics) can collect this flow. Notifications never reach the EffectHandler.
   *
   * For features with no notifications, this flow will never emit (Notification type is [Nothing]).
   */
  public val notifications: Flow<Notification>

  /**
   * Sends an action to be processed by the feature's reducer.
   *
   * Actions are queued and processed sequentially. Each action causes:
   * 1. The reducer to compute a new state, optional commands, and optional notifications
   * 2. The state to be updated
   * 3. Commands to be sent to the effect handler
   * 4. Notifications to be emitted to external observers
   *
   * @param action The action to process
   */
  public fun send(action: Action)

  /**
   * Starts the feature's action and command processing loops.
   *
   * Must be called before the feature can process actions. The feature will continue
   * processing until the provided [scope] is cancelled.
   *
   * @param scope The coroutine scope in which to run the processing loops
   */
  public fun launchIn(scope: CoroutineScope)
}

/**
 * A pure function that computes a new state, commands, and notifications from an action and current state.
 *
 * Reducers must be pure functions with no side effects. They should:
 * - Always return the same result for the same inputs
 * - Not modify any external state
 * - Not perform I/O operations
 *
 * Use the [reducer] DSL function to create reducers with a convenient syntax:
 * ```
 * val myReducer: Reducer<MyAction, MyState, MyCommand, MyNotification> = reducer { action, state ->
 *   when (action) {
 *     is MyAction.Increment -> state { state.copy(count = state.count + 1) }
 *     is MyAction.LoadData -> command(MyCommand.FetchData)
 *     is MyAction.Completed -> notify(MyNotification.Finished)
 *   }
 * }
 * ```
 *
 * For features with only commands and no notifications, use [Nothing] as Notification type:
 * ```
 * val myReducer: Reducer<MyAction, MyState, MyCommand, Nothing> = reducer { action, state ->
 *   when (action) {
 *     is MyAction.LoadData -> command(MyCommand.FetchData)
 *     // notify(...) cannot be called - compile error
 *   }
 * }
 * ```
 *
 * @param Action The type of actions the reducer handles
 * @param State The type of state the reducer operates on
 * @param Command The type of commands the reducer can produce for EffectHandler
 * @param Notification The type of notifications the reducer can produce for coordinators/UI
 */
public typealias Reducer<Action, State, Command, Notification> =
  (Action, State) -> ReducerResult<State, Command, Notification>

/**
 * A function that handles commands (internal side effects) and produces actions in response.
 *
 * Effect handlers are responsible for:
 * - Performing asynchronous operations (API calls, database queries, etc.)
 * - Converting command results into actions to update the state
 *
 * The handler processes only commands - notifications are handled externally by coordinators.
 * The handler returns a [Flow] of actions, allowing it to emit multiple actions
 * for a single command (e.g., progress updates, success/failure results).
 *
 * Example:
 * ```
 * val effectHandler: EffectHandler<MyCommand, MyAction> = { command ->
 *   when (command) {
 *     is MyCommand.FetchData -> flow {
 *       val result = api.fetchData()
 *       emit(MyAction.DataLoaded(result))
 *     }
 *     is MyCommand.SaveToDb -> flow {
 *       db.save(command.data)
 *       emit(MyAction.Saved)
 *     }
 *   }
 * }
 * ```
 *
 * @param Command The type of commands this handler processes
 * @param Action The type of actions this handler produces
 */
public typealias EffectHandler<Command, Action> = (Command) -> Flow<Action>

/**
 * Container for commands and notifications produced by a reducer.
 *
 * @property commands Commands to be processed internally by the EffectHandler
 * @property notifications Notifications to be emitted to external observers
 */
public data class Effects<Command, Notification>(
  val commands: List<Command>,
  val notifications: List<Notification>,
)

/**
 * The result of a reducer invocation, containing the new state and effects.
 *
 * Effects are split into commands (processed internally by EffectHandler) and
 * notifications (exposed to external observers like coordinators and UI).
 *
 * @property state The new state after processing the action
 * @property effects Container with commands and notifications
 */
public data class ReducerResult<State, Command, Notification>(
  val state: State,
  val effects: Effects<Command, Notification>,
) {
  // Convenience accessors for direct access
  val commands: List<Command> get() = effects.commands
  val notifications: List<Notification> get() = effects.notifications
}
