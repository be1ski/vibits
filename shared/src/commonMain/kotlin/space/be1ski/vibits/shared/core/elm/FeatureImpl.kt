@file:Suppress("RedundantVisibilityModifier")

package space.be1ski.vibits.shared.core.elm

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.DEFAULT_CONCURRENCY
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * @param concurrency The number of concurrent coroutines to process effects. Defaults to [DEFAULT_CONCURRENCY].
 * If you have a lot of observers in your [EffectHandler], consider increasing this value.
 */
@OptIn(FlowPreview::class)
public open class FeatureImpl<Action, State, Effect>(
  initialState: State,
  private val reducer: Reducer<Action, State, Effect>,
  private val effectHandler: EffectHandler<Effect, Action>,
  private val initialEffects: List<Effect> = emptyList(),
  private val concurrency: Int = DEFAULT_CONCURRENCY,
) : Feature<Action, State, Effect> {
  private val _state: MutableStateFlow<State> = MutableStateFlow(initialState)
  override val state: StateFlow<State> = _state.asStateFlow()

  private val internalEffects: Channel<Effect> = Channel(Channel.UNLIMITED)

  private val _effects: Channel<Effect> = Channel(Channel.UNLIMITED)
  override val effects: Flow<Effect> = _effects.receiveAsFlow()

  private val actions: Channel<Action> = Channel(Channel.UNLIMITED)

  @OptIn(ExperimentalCoroutinesApi::class)
  override fun launchIn(scope: CoroutineScope) {
    actions
      .receiveAsFlow()
      .onEach(::processAction)
      .launchIn(scope)

    internalEffects
      .receiveAsFlow()
      .flatMapMerge(transform = effectHandler::invoke, concurrency = concurrency)
      .onEach(::send)
      .launchIn(scope)

    initialEffects.forEach(::sendEffect)
  }

  override fun send(action: Action) {
    actions.trySend(action)
  }

  private fun processAction(action: Action) {
    val (newState, effects) = reducer.invoke(action, state.value)

    _state.value = newState

    effects.forEach(::sendEffect)
  }

  private fun sendEffect(effect: Effect) {
    internalEffects.trySend(effect)
    _effects.trySend(effect)
  }
}
