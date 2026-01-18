package space.be1ski.vibits.shared.core.elm

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class FeatureImplTest {
  private data class State(
    val count: Int = 0,
    val loading: Boolean = false,
  )

  private sealed interface Action {
    data object Increment : Action

    data object Decrement : Action

    data object StartLoading : Action

    data object LoadingComplete : Action

    data class SetCount(
      val value: Int,
    ) : Action
  }

  private sealed interface Effect {
    data object Load : Effect

    data object NoOp : Effect
  }

  private val testReducer: Reducer<Action, State, Effect> =
    reducer { action, _ ->
      when (action) {
        Action.Increment -> state { copy(count = count + 1) }
        Action.Decrement -> state { copy(count = count - 1) }
        Action.StartLoading -> {
          state { copy(loading = true) }
          effect(Effect.Load)
        }
        Action.LoadingComplete -> state { copy(loading = false) }
        is Action.SetCount -> state { copy(count = action.value) }
      }
    }

  private fun createEffectHandler(loadResult: Int = 42): EffectHandler<Effect, Action> =
    { effect ->
      when (effect) {
        Effect.Load -> flowOf(Action.SetCount(loadResult), Action.LoadingComplete)
        Effect.NoOp -> emptyFlow()
      }
    }

  @Test
  fun `when feature created then initial state is set`() =
    runTest {
      val feature =
        FeatureImpl(
          initialState = State(count = 10),
          reducer = testReducer,
          effectHandler = createEffectHandler(),
        )

      assertEquals(State(count = 10), feature.state.value)
    }

  @Test
  fun `when action sent then state is updated`() =
    runTest(UnconfinedTestDispatcher()) {
      val feature =
        FeatureImpl(
          initialState = State(),
          reducer = testReducer,
          effectHandler = createEffectHandler(),
        )
      val featureScope = featureTestScope()
      feature.launchIn(featureScope)

      feature.send(Action.Increment)

      assertEquals(1, feature.state.value.count)
      featureScope.cancel()
    }

  @Test
  fun `when multiple actions sent then state reflects all changes`() =
    runTest(UnconfinedTestDispatcher()) {
      val feature =
        FeatureImpl(
          initialState = State(),
          reducer = testReducer,
          effectHandler = createEffectHandler(),
        )
      val featureScope = featureTestScope()
      feature.launchIn(featureScope)

      feature.send(Action.Increment)
      feature.send(Action.Increment)
      feature.send(Action.Increment)
      feature.send(Action.Decrement)

      assertEquals(2, feature.state.value.count)
      featureScope.cancel()
    }

  @Test
  fun `when action triggers effect then effect handler processes it`() =
    runTest(UnconfinedTestDispatcher()) {
      val feature =
        FeatureImpl(
          initialState = State(),
          reducer = testReducer,
          effectHandler = createEffectHandler(loadResult = 100),
        )
      val featureScope = featureTestScope()
      feature.launchIn(featureScope)

      feature.send(Action.StartLoading)

      assertEquals(100, feature.state.value.count)
      assertEquals(false, feature.state.value.loading)
      featureScope.cancel()
    }

  @Test
  fun `when initial effects provided then they are processed on launch`() =
    runTest(UnconfinedTestDispatcher()) {
      val feature =
        FeatureImpl(
          initialState = State(),
          reducer = testReducer,
          effectHandler = createEffectHandler(loadResult = 50),
          initialEffects = listOf(Effect.Load),
        )
      val featureScope = featureTestScope()
      feature.launchIn(featureScope)

      assertEquals(50, feature.state.value.count)
      featureScope.cancel()
    }

  @Test
  fun `when effect handler returns multiple actions then all are processed`() =
    runTest(UnconfinedTestDispatcher()) {
      var actionCount = 0
      val countingReducer: Reducer<Action, State, Effect> =
        reducer { action, _ ->
          actionCount++
          when (action) {
            Action.Increment -> state { copy(count = count + 1) }
            Action.StartLoading -> {
              state { copy(loading = true) }
              effect(Effect.Load)
            }
            else -> {}
          }
        }

      val feature =
        FeatureImpl<Action, State, Effect>(
          initialState = State(),
          reducer = countingReducer,
          effectHandler = { effect ->
            when (effect) {
              Effect.Load -> flowOf(Action.Increment, Action.Increment, Action.Increment)
              else -> emptyFlow()
            }
          },
        )
      val featureScope = featureTestScope()
      feature.launchIn(featureScope)

      feature.send(Action.StartLoading)

      // 1 StartLoading + 3 Increment = 4 actions
      assertEquals(4, actionCount)
      assertEquals(3, feature.state.value.count)
      featureScope.cancel()
    }
}
