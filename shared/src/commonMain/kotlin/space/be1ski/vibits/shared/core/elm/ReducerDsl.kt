@file:Suppress("RedundantVisibilityModifier")

package space.be1ski.vibits.shared.core.elm

/**
 * Creates a [Reducer] using the DSL syntax.
 *
 * Simple example:
 * ```
 * val myReducer = reducer<MyAction, MyState, MyEffect> { action, state ->
 *   when (action) {
 *     is MyAction.IncrementCounter -> state { copy(counter = counter++) }
 *     is MyAction.Reset -> {
 *       state { copy(counter = 0) }
 *       effect(MyEffect.ShowResetNotification)
 *     }
 *     is MyAction.LoadData -> effect(MyEffect.FetchData)
 *     is MyAction.WithEffects -> effects(MyEffect.FetchData, MyEffect.ShowLoading, MyEffect.LogAnalytics)
 *   }
 * }
 * ```
 *
 * Example when reducer is extracted to a different file:
 * ```
 * class MyReducer : Reducer<MyAction, MyState, MyEffect> by reducer({ action, state ->
 *   when (action) {
 *     is MyAction.IncrementCounter -> handleReset(state)
 *     ...
 *   }
 * })
 *
 * fun ReducerContext<MyState, MyEffect>.handleReset(state: MyState) {
 *   state { copy(counter = 0) }
 *   effect(MyEffect.ShowResetNotification)
 * }
 * ```
 */
public fun <Action, State, Effect> reducer(reduce: ReducerContext<State, Effect>.(Action, State) -> Unit): Reducer<Action, State, Effect> =
  { action, state ->
    ReducerContext<State, Effect>().apply { reduce(action, state) }.getResult(state)
  }

/**
 * Builder context for creating [ReducerResult] instances in a DSL-style.
 *
 * This class is used within the [reducer] function to provide a convenient way to build
 * state updates and collect effects. It supports multiple patterns:
 *
 * - State update only: `state { copy(count = count + 1) }`
 * - Effect only: `effect(MyEffect.LogAnalytics)`
 * - State and effects: combine both in any order
 * - Multiple effects: `effects(Effect.A, Effect.B)` or `effects(listOf(...))`
 *
 * Note: If [state] is called multiple times, only the last transformation is applied.
 * Effects accumulate with each call to [effect] or [effects].
 */
public class ReducerContext<State, Effect> {
  private var stateUpdate: (State.() -> State) = { this }
  private val effects = mutableListOf<Effect>()

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
   * Adds a single effect to be executed.
   *
   * ```
   * effect(MyEffect.FetchData)
   * ```
   *
   * @param effect The effect to add
   */
  public fun effect(effect: Effect) {
    effects.add(effect)
  }

  /**
   * Adds multiple effects to be executed.
   *
   * ```
   * effects(MyEffect.SaveData, MyEffect.ShowNotification, MyEffect.LogAnalytics)
   * ```
   *
   * @param effects The effects to add
   */
  public fun effects(vararg effects: Effect) {
    effects(effects.toList())
  }

  /**
   * Adds a list of effects to be executed.
   *
   * ```
   * effects(listOf(MyEffect.SaveData, MyEffect.ShowNotification))
   * ```
   *
   * @param effects The list of effects to add
   */
  public fun effects(effects: List<Effect>) {
    this.effects.addAll(effects)
  }

  internal fun getResult(initialState: State): ReducerResult<State, Effect> =
    ReducerResult(state = stateUpdate(initialState), effects = effects)
}
