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
 * Default implementation of [Feature] that processes actions and effects using channels and flows.
 *
 * This implementation uses unbounded channels for actions and effects, ensuring that no events
 * are lost even under high load. Actions are processed sequentially, while effects can be
 * processed concurrently (controlled by [concurrency]).
 *
 * Example usage:
 * ```
 * val feature = FeatureImpl(
 *   initialState = MyState(),
 *   reducer = myReducer,
 *   effectHandler = myEffectHandler,
 *   initialEffects = listOf(MyEffect.LoadInitialData),
 * )
 *
 * feature.launchIn(viewModelScope)
 * feature.send(MyAction.ButtonClicked)
 * ```
 *
 * @param initialState The initial state of the feature
 * @param reducer The reducer that processes actions and produces state changes and effects
 * @param effectHandler The handler that processes effects and produces actions
 * @param initialEffects Effects to process immediately when [launchIn] is called
 * @param concurrency The number of concurrent coroutines to process effects.
 *   Defaults to [DEFAULT_CONCURRENCY]. Increase this value if your effect handler
 *   performs many parallel operations.
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

  private val actionChannel: Channel<Action> = Channel(Channel.UNLIMITED)
  private val effectQueue: Channel<Effect> = Channel(Channel.UNLIMITED)
  private val effectBroadcast: Channel<Effect> = Channel(Channel.UNLIMITED)
  override val effects: Flow<Effect> = effectBroadcast.receiveAsFlow()

  @OptIn(ExperimentalCoroutinesApi::class)
  override fun launchIn(scope: CoroutineScope) {
    actionChannel
      .receiveAsFlow()
      .onEach(::dispatch)
      .launchIn(scope)

    effectQueue
      .receiveAsFlow()
      .flatMapMerge(concurrency) { effectHandler(it) }
      .onEach(::send)
      .launchIn(scope)

    initialEffects.forEach(::enqueueEffect)
  }

  override fun send(action: Action) {
    actionChannel.trySend(action)
  }

  private fun dispatch(action: Action) {
    val (newState, newEffects) = reducer(action, state.value)
    _state.value = newState
    newEffects.forEach(::enqueueEffect)
  }

  private fun enqueueEffect(effect: Effect) {
    effectQueue.trySend(effect)
    effectBroadcast.trySend(effect)
  }
}
