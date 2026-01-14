package space.be1ski.memos.shared.elm

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

public interface Feature<Action, State, Effect> {
  public val state: StateFlow<State>
  public val effects: Flow<Effect>
  public fun send(action: Action)
  public fun launchIn(scope: CoroutineScope)
}

public typealias Reducer<Action, State, Effect> = (Action, State) -> ReducerResult<State, Effect>
public typealias EffectHandler<Effect, Action> = (Effect) -> Flow<Action>

public data class ReducerResult<State, Effect>(val state: State, val effects: List<Effect>)
