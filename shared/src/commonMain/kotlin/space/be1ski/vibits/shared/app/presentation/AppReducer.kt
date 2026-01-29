package space.be1ski.vibits.shared.app.presentation

import space.be1ski.vibits.shared.app.domain.model.AppState
import space.be1ski.vibits.shared.app.domain.model.Screen
import space.be1ski.vibits.shared.app.domain.usecase.AdjustDateForTabChangeUseCase
import space.be1ski.vibits.shared.app.domain.usecase.GetActivityRangeStartDateUseCase
import space.be1ski.vibits.shared.core.elm.Reducer
import space.be1ski.vibits.shared.core.elm.reducer
import space.be1ski.vibits.shared.feature.settings.domain.model.TimeRangeTab

/**
 * Pure reducer for the App coordinator feature.
 */
internal val appReducer: Reducer<AppAction, AppState, AppEffect, Nothing> =
  reducer { action, state ->
    when (action) {
      is AppAction.SelectScreen -> {
        state { copy(selectedScreen = action.screen) }
      }

      is AppAction.SetHabitsTimeRangeTab -> {
        state { copy(habitsTimeRangeTab = action.tab) }
        command(AppEffect.SaveHabitsTimeRangeTab(action.tab))
      }

      is AppAction.SetPostsTimeRangeTab -> {
        state { copy(postsTimeRangeTab = action.tab) }
        command(AppEffect.SavePostsTimeRangeTab(action.tab))
      }

      is AppAction.SetPeriodStartDate -> {
        state { copy(periodStartDate = action.date) }
      }

      is AppAction.SetActivityRange -> {
        state { copy(periodStartDate = GetActivityRangeStartDateUseCase(action.range)) }
      }

      is AppAction.ChangeHabitsTab -> {
        val adjustedDate = AdjustDateForTabChangeUseCase(state.periodStartDate, action.oldTab, action.newTab)
        state { copy(habitsTimeRangeTab = action.newTab, periodStartDate = adjustedDate) }
        command(AppEffect.SaveHabitsTimeRangeTab(action.newTab))
      }

      is AppAction.ChangePostsTab -> {
        val adjustedDate = AdjustDateForTabChangeUseCase(state.periodStartDate, action.oldTab, action.newTab)
        state { copy(postsTimeRangeTab = action.newTab, periodStartDate = adjustedDate) }
        command(AppEffect.SavePostsTimeRangeTab(action.newTab))
      }

      is AppAction.ResetToHome -> {
        state {
          when (selectedScreen) {
            Screen.HABITS -> copy(periodStartDate = action.today, habitsTimeRangeTab = TimeRangeTab.WEEKS)
            Screen.STATS -> copy(periodStartDate = action.today, postsTimeRangeTab = TimeRangeTab.WEEKS)
            Screen.FEED -> copy(periodStartDate = action.today)
          }
        }
      }

      is AppAction.SetAppMode -> {
        state { copy(appMode = action.mode) }
      }

      is AppAction.MarkAutoLoaded -> {
        state { copy(autoLoaded = true) }
      }

      is AppAction.SetPostsListExpanded -> {
        state { copy(postsListExpanded = action.expanded) }
      }
    }
  }
