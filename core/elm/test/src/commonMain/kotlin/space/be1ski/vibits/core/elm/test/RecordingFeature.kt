package space.be1ski.vibits.core.elm.test

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import space.be1ski.vibits.core.elm.Feature

class RecordingFeature<Action, State, Command, Notification>(
  initialState: State,
) : Feature<Action, State, Command, Notification> {
  private val _state = MutableStateFlow(initialState)
  override val state: StateFlow<State> = _state.asStateFlow()

  override val notifications: Flow<Notification> = emptyFlow()

  private val _actions = mutableListOf<Action>()
  val actions: List<Action> get() = _actions.toList()

  override fun send(action: Action) {
    _actions.add(action)
  }

  override fun launchIn(scope: CoroutineScope) = Unit

  fun setState(newState: State) {
    _state.value = newState
  }
}
