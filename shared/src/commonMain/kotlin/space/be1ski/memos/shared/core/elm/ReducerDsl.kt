package space.be1ski.memos.shared.core.elm

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
public fun <Action, State, Effect> reducer(
  reduce: ReducerContext<State, Effect>.(Action, State) -> Unit
): Reducer<Action, State, Effect> {
  return { action, state -> ReducerContext<State, Effect>().apply { reduce(action, state) }.getResult(state) }
}

/**
 * Class for creating [ReducerResult] instances in a DSL-style.
 *
 * This class is used within the [reducer] function to provide a convenient way to build
 * state updates and collect effects.
 */
public class ReducerContext<State, Effect> {
  private var stateUpdate: (State.() -> State) = { this }
  private val effects = mutableListOf<Effect>()

  public fun state(update: State.() -> State) {
    stateUpdate = update
  }

  public fun state(newState: State) {
    state { newState }
  }

  public fun effect(effect: Effect) {
    effects.add(effect)
  }

  public fun effects(vararg effects: Effect) {
    effects(effects.toList())
  }

  public fun effects(effects: List<Effect>) {
    this.effects.addAll(effects)
  }

  internal fun getResult(initialState: State): ReducerResult<State, Effect> {
    return ReducerResult(state = stateUpdate(initialState), effects = effects)
  }
}
