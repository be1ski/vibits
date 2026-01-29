package space.be1ski.vibits.shared.feature.habits.presentation.reducer

import space.be1ski.vibits.shared.core.elm.ReducerResult
import space.be1ski.vibits.shared.core.elm.reducer
import space.be1ski.vibits.shared.feature.habits.presentation.ActivityCacheKey
import space.be1ski.vibits.shared.feature.habits.presentation.CachedActivityData
import space.be1ski.vibits.shared.feature.habits.presentation.action.HabitsAction
import space.be1ski.vibits.shared.feature.habits.presentation.effect.HabitsEffect
import space.be1ski.vibits.shared.feature.habits.presentation.state.HabitsState

/**
 * Sub-reducer for cache management.
 */
internal fun cacheReducer(
  action: HabitsAction.Cache,
  state: HabitsState,
): ReducerResult<HabitsState, HabitsEffect, Nothing> =
  reducer<HabitsAction.Cache, HabitsState, HabitsEffect, Nothing> { a, s ->
    when (a) {
      is HabitsAction.Cache.RequestPrewarmAllRanges -> {
        state {
          copy(
            needsCacheRefresh = false,
            isInitialLoading = true,
          )
        }
        command(HabitsEffect.RunPrewarmAllRanges(a.memos, a.appMode))
      }

      is HabitsAction.Cache.UpdateActivityData -> {
        val key = ActivityCacheKey(a.range, a.mode, a.appMode)
        state {
          copy(
            activityDataCache =
              activityDataCache +
                (key to CachedActivityData(a.weekData, a.configTimeline, a.successRate)),
            isRecalculating = isRecalculating - key,
          )
        }
      }

      is HabitsAction.Cache.PrewarmCompleted -> {
        state {
          copy(
            isInitialLoading = false,
          )
        }
      }

      is HabitsAction.Cache.InvalidateAllCache -> {
        state {
          copy(
            activityDataCache = emptyMap(),
            isRecalculating = emptySet(),
            needsCacheRefresh = true,
          )
        }
      }

      is HabitsAction.Cache.InvalidateCache -> {
        state {
          copy(
            isRecalculating = isRecalculating + ActivityCacheKey(a.range, a.mode, a.appMode),
            needsCacheRefresh = false,
          )
        }
        command(HabitsEffect.RecalculateActivityData(a.range, a.mode, a.appMode, a.memos))
      }
    }
  }(action, state)
