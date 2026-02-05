package space.be1ski.vibits.feature.homescreen.presentation.effect

import space.be1ski.vibits.feature.settings.domain.model.TimeRangeTab

/**
 * Side effects for the App coordinator feature.
 */
sealed interface AppEffect {
  data class SaveHabitsTimeRangeTab(
    val tab: TimeRangeTab,
  ) : AppEffect

  data class SavePostsTimeRangeTab(
    val tab: TimeRangeTab,
  ) : AppEffect
}
