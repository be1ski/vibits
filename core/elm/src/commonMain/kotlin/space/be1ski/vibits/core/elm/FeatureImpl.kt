@file:Suppress("RedundantVisibilityModifier")

package space.be1ski.vibits.core.elm

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
 * Default implementation of [Feature] that processes actions, commands, and notifications using channels and flows.
 *
 * This implementation uses unbounded channels for actions, commands, and notifications, ensuring that no events
 * are lost even under high load. Actions are processed sequentially, while commands can be
 * processed concurrently (controlled by [concurrency]).
 *
 * Commands are processed internally by the EffectHandler. Notifications are exposed to external
 * observers (coordinators, UI) and never reach the EffectHandler.
 *
 * Example usage:
 * ```
 * val feature = FeatureImpl(
 *   initialState = MyState(),
 *   reducer = myReducer,
 *   effectHandler = myEffectHandler,
 *   initialCommands = listOf(MyCommand.LoadInitialData),
 * )
 *
 * feature.launchIn(viewModelScope)
 * feature.send(MyAction.ButtonClicked)
 * feature.notifications.collect { notification -> handleNotification(notification) }
 * ```
 *
 * @param initialState The initial state of the feature
 * @param reducer The reducer that processes actions and produces state changes, commands, and notifications
 * @param effectHandler The handler that processes commands and produces actions
 * @param initialCommands Commands to process immediately when [launchIn] is called
 * @param concurrency The number of concurrent coroutines to process commands.
 *   Defaults to [DEFAULT_CONCURRENCY]. Increase this value if your effect handler
 *   performs many parallel operations.
 */
@OptIn(FlowPreview::class)
public open class FeatureImpl<Action, State, Command, Notification>(
  initialState: State,
  private val reducer: Reducer<Action, State, Command, Notification>,
  private val effectHandler: EffectHandler<Command, Action>,
  private val initialCommands: List<Command> = emptyList(),
  private val concurrency: Int = DEFAULT_CONCURRENCY,
) : Feature<Action, State, Command, Notification> {
  private val _state: MutableStateFlow<State> = MutableStateFlow(initialState)
  override val state: StateFlow<State> = _state.asStateFlow()

  private val actionChannel: Channel<Action> = Channel(Channel.UNLIMITED)
  private val commandQueue: Channel<Command> = Channel(Channel.UNLIMITED)
  private val notificationBroadcast: Channel<Notification> = Channel(Channel.UNLIMITED)
  override val notifications: Flow<Notification> = notificationBroadcast.receiveAsFlow()

  @OptIn(ExperimentalCoroutinesApi::class)
  override fun launchIn(scope: CoroutineScope) {
    actionChannel
      .receiveAsFlow()
      .onEach(::dispatch)
      .launchIn(scope)

    commandQueue
      .receiveAsFlow()
      .flatMapMerge(concurrency) { effectHandler(it) }
      .onEach(::send)
      .launchIn(scope)

    initialCommands.forEach(::enqueueCommand)
  }

  override fun send(action: Action) {
    actionChannel.trySend(action)
  }

  private fun dispatch(action: Action) {
    val (newState, effects) = reducer(action, state.value)
    _state.value = newState
    effects.commands.forEach(::enqueueCommand)
    effects.notifications.forEach(::emitNotification)
  }

  private fun enqueueCommand(command: Command) {
    commandQueue.trySend(command)
  }

  private fun emitNotification(notification: Notification) {
    notificationBroadcast.trySend(notification)
  }
}
