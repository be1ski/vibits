package space.be1ski.vibits.shared.app.presentation

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import space.be1ski.vibits.shared.core.elm.EffectHandler
import space.be1ski.vibits.shared.feature.settings.domain.usecase.SaveTimeRangeTabUseCase
import space.be1ski.vibits.shared.feature.settings.domain.usecase.TimeRangeScreen

internal class AppEffectHandler(
  private val saveTimeRangeTab: SaveTimeRangeTabUseCase,
) : EffectHandler<AppEffect, AppAction> {
  override fun invoke(effect: AppEffect): Flow<AppAction> {
    when (effect) {
      is AppEffect.SaveHabitsTimeRangeTab -> {
        saveTimeRangeTab(TimeRangeScreen.HABITS, effect.tab)
      }
      is AppEffect.SavePostsTimeRangeTab -> {
        saveTimeRangeTab(TimeRangeScreen.POSTS, effect.tab)
      }
    }
    return emptyFlow()
  }
}
