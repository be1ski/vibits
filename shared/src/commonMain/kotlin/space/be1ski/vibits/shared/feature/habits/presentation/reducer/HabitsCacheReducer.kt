package space.be1ski.vibits.shared.feature.habits.presentation.reducer

import space.be1ski.vibits.shared.core.elm.Reducer
import space.be1ski.vibits.shared.core.elm.reducer
import space.be1ski.vibits.shared.feature.habits.presentation.ActivityCacheKey
import space.be1ski.vibits.shared.feature.habits.presentation.CachedActivityData
import space.be1ski.vibits.shared.feature.habits.presentation.action.HabitsAction
import space.be1ski.vibits.shared.feature.habits.presentation.effect.HabitsEffect
import space.be1ski.vibits.shared.feature.habits.presentation.state.HabitsState

/**
 * Sub-reducer for cache management.
 */
internal val cacheReducer: Reducer<HabitsAction.Cache, HabitsState, HabitsEffect, Nothing> =
  reducer { action, state ->
    when (action) {
      is HabitsAction.Cache.RequestPrewarmAllRanges -> {
        state {
          copy(
            needsCacheRefresh = false,
            isInitialLoading = true,
          )
        }
        command(HabitsEffect.RunPrewarmAllRanges(action.memos, action.appMode))
      }

      is HabitsAction.Cache.UpdateActivityData -> {
        val key = ActivityCacheKey(action.range, action.mode, action.appMode)
        state {
          copy(
            activityDataCache =
              activityDataCache +
                (key to CachedActivityData(action.weekData, action.configTimeline, action.successRate)),
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
            isRecalculating = isRecalculating + ActivityCacheKey(action.range, action.mode, action.appMode),
            needsCacheRefresh = false,
          )
        }
        command(HabitsEffect.RecalculateActivityData(action.range, action.mode, action.appMode, action.memos))
      }
    }
  }
