@file:Suppress("RedundantVisibilityModifier")

package space.be1ski.vibits.shared.core.elm

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * A Feature represents a self-contained unit of application logic following The Elm Architecture (TEA).
 *
 * The Elm Architecture is a unidirectional data flow pattern where:
 * - [State] represents the current state of the feature
 * - [Action] represents events that can modify the state
 * - [Effect] represents side effects (API calls, navigation, etc.)
 *
 * Data flows in one direction:
 * ```
 * Action -> Reducer -> (State, Effects) -> EffectHandler -> Action -> ...
 * ```
 *
 * Example usage:
 * ```
 * val feature = FeatureImpl(
 *   initialState = CounterState(count = 0),
 *   reducer = counterReducer,
 *   effectHandler = counterEffectHandler,
 * )
 *
 * // Start processing actions and effects
 * feature.launchIn(viewModelScope)
 *
 * // Observe state
 * feature.state.collect { state -> updateUI(state) }
 *
 * // Send actions
 * feature.send(CounterAction.Increment)
 * ```
 *
 * @param Action The type of actions this feature accepts
 * @param State The type of state this feature manages
 * @param Effect The type of side effects this feature produces
 */
public interface Feature<Action, State, Effect> {
  /**
   * The current state of the feature as a [StateFlow].
   *
   * Observers can collect this flow to receive state updates.
   * The flow always has a current value (hot flow).
   */
  public val state: StateFlow<State>

  /**
   * A [Flow] of effects produced by the feature.
   *
   * Effects are emitted when the reducer returns them. External observers (e.g., for analytics
   * or one-time UI events) can collect this flow. Note that effects are also processed internally
   * by the effect handler.
   */
  public val effects: Flow<Effect>

  /**
   * Sends an action to be processed by the feature's reducer.
   *
   * Actions are queued and processed sequentially. Each action causes:
   * 1. The reducer to compute a new state and optional effects
   * 2. The state to be updated
   * 3. Effects to be sent to the effect handler
   *
   * @param action The action to process
   */
  public fun send(action: Action)

  /**
   * Starts the feature's action and effect processing loops.
   *
   * Must be called before the feature can process actions. The feature will continue
   * processing until the provided [scope] is cancelled.
   *
   * @param scope The coroutine scope in which to run the processing loops
   */
  public fun launchIn(scope: CoroutineScope)
}

/**
 * A pure function that computes a new state and effects from an action and current state.
 *
 * Reducers must be pure functions with no side effects. They should:
 * - Always return the same result for the same inputs
 * - Not modify any external state
 * - Not perform I/O operations
 *
 * Use the [reducer] DSL function to create reducers with a convenient syntax:
 * ```
 * val myReducer: Reducer<MyAction, MyState, MyEffect> = reducer { action, state ->
 *   when (action) {
 *     is MyAction.Increment -> state { copy(count = count + 1) }
 *     is MyAction.LoadData -> effect(MyEffect.FetchData)
 *   }
 * }
 * ```
 *
 * @param Action The type of actions the reducer handles
 * @param State The type of state the reducer operates on
 * @param Effect The type of effects the reducer can produce
 */
public typealias Reducer<Action, State, Effect> = (Action, State) -> ReducerResult<State, Effect>

/**
 * A function that handles side effects and produces actions in response.
 *
 * Effect handlers are responsible for:
 * - Performing asynchronous operations (API calls, database queries, etc.)
 * - Converting effect results into actions to update the state
 *
 * The handler returns a [Flow] of actions, allowing it to emit multiple actions
 * for a single effect (e.g., progress updates, success/failure results).
 *
 * Example:
 * ```
 * val effectHandler: EffectHandler<MyEffect, MyAction> = { effect ->
 *   when (effect) {
 *     is MyEffect.FetchData -> flow {
 *       val result = api.fetchData()
 *       emit(MyAction.DataLoaded(result))
 *     }
 *     is MyEffect.LogAnalytics -> {
 *       analytics.log(effect.event)
 *       emptyFlow()
 *     }
 *   }
 * }
 * ```
 *
 * @param Effect The type of effects this handler processes
 * @param Action The type of actions this handler produces
 */
public typealias EffectHandler<Effect, Action> = (Effect) -> Flow<Action>

/**
 * The result of a reducer invocation, containing the new state and any effects to execute.
 *
 * @property state The new state after processing the action
 * @property effects A list of effects to be processed by the effect handler
 */
public data class ReducerResult<State, Effect>(
  val state: State,
  val effects: List<Effect>,
)
