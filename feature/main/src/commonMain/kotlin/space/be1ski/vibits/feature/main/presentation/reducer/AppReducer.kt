package space.be1ski.vibits.feature.main.presentation.reducer

import space.be1ski.vibits.core.elm.Reducer
import space.be1ski.vibits.core.elm.reducer
import space.be1ski.vibits.feature.main.domain.model.AppState
import space.be1ski.vibits.feature.main.domain.model.Screen
import space.be1ski.vibits.feature.main.domain.usecase.AdjustDateForTabChangeUseCase
import space.be1ski.vibits.feature.main.domain.usecase.GetActivityRangeStartDateUseCase
import space.be1ski.vibits.feature.main.presentation.action.AppAction
import space.be1ski.vibits.feature.main.presentation.effect.AppEffect
import space.be1ski.vibits.feature.settings.domain.model.TimeRangeTab

/**
 * Pure reducer for the App coordinator feature.
 */
internal val appReducer: Reducer<AppAction, AppState, AppEffect, Nothing> =
  reducer { action, state ->
    when (action) {
      is AppAction.SelectScreen -> {
        state { state.copy(selectedScreen = action.screen) }
      }

      is AppAction.SetHabitsTimeRangeTab -> {
        state { state.copy(habitsTimeRangeTab = action.tab) }
        command(AppEffect.SaveHabitsTimeRangeTab(action.tab))
      }

      is AppAction.SetPostsTimeRangeTab -> {
        state { state.copy(postsTimeRangeTab = action.tab) }
        command(AppEffect.SavePostsTimeRangeTab(action.tab))
      }

      is AppAction.SetPeriodStartDate -> {
        state { state.copy(periodStartDate = action.date) }
      }

      is AppAction.SetActivityRange -> {
        state { state.copy(periodStartDate = GetActivityRangeStartDateUseCase(action.range)) }
      }

      is AppAction.ChangeHabitsTab -> {
        val adjustedDate = AdjustDateForTabChangeUseCase(state.periodStartDate, action.oldTab, action.newTab)
        state { state.copy(habitsTimeRangeTab = action.newTab, periodStartDate = adjustedDate) }
        command(AppEffect.SaveHabitsTimeRangeTab(action.newTab))
      }

      is AppAction.ChangePostsTab -> {
        val adjustedDate = AdjustDateForTabChangeUseCase(state.periodStartDate, action.oldTab, action.newTab)
        state { state.copy(postsTimeRangeTab = action.newTab, periodStartDate = adjustedDate) }
        command(AppEffect.SavePostsTimeRangeTab(action.newTab))
      }

      is AppAction.ResetToHome -> {
        state {
          when (state.selectedScreen) {
            Screen.HABITS -> state.copy(periodStartDate = action.today, habitsTimeRangeTab = TimeRangeTab.WEEKS)
            Screen.STATS -> state.copy(periodStartDate = action.today, postsTimeRangeTab = TimeRangeTab.WEEKS)
            Screen.FEED -> state.copy(periodStartDate = action.today)
          }
        }
      }

      is AppAction.SetAppMode -> {
        state { state.copy(appMode = action.mode) }
      }

      is AppAction.MarkAutoLoaded -> {
        state { state.copy(autoLoaded = true) }
      }

      is AppAction.SetPostsListExpanded -> {
        state { state.copy(postsListExpanded = action.expanded) }
      }
    }
  }
