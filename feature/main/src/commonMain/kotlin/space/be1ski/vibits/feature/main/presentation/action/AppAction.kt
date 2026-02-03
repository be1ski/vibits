package space.be1ski.vibits.feature.main.presentation.action

import kotlinx.datetime.LocalDate
import space.be1ski.vibits.core.elm.Action
import space.be1ski.vibits.core.platform.mode.AppMode
import space.be1ski.vibits.feature.habits.domain.model.ActivityRange
import space.be1ski.vibits.feature.main.domain.model.Screen
import space.be1ski.vibits.feature.settings.domain.model.TimeRangeTab

/**
 * Actions for the App coordinator feature.
 */
sealed interface AppAction : Action {
  // Navigation
  data class SelectScreen(
    val screen: Screen,
  ) : AppAction

  data class SetHabitsTimeRangeTab(
    val tab: TimeRangeTab,
  ) : AppAction

  data class SetPostsTimeRangeTab(
    val tab: TimeRangeTab,
  ) : AppAction

  data class SetPeriodStartDate(
    val date: LocalDate,
  ) : AppAction

  data class SetActivityRange(
    val range: ActivityRange,
  ) : AppAction

  data class ChangeHabitsTab(
    val oldTab: TimeRangeTab,
    val newTab: TimeRangeTab,
  ) : AppAction

  data class ChangePostsTab(
    val oldTab: TimeRangeTab,
    val newTab: TimeRangeTab,
  ) : AppAction

  data class ResetToHome(
    val today: LocalDate,
  ) : AppAction

  // Mode
  data class SetAppMode(
    val mode: AppMode,
  ) : AppAction

  // UI state
  data object MarkAutoLoaded : AppAction

  data class SetPostsListExpanded(
    val expanded: Boolean,
  ) : AppAction
}
