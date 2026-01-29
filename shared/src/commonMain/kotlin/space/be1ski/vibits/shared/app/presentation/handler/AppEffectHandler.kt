package space.be1ski.vibits.shared.app.presentation.handler

import kotlinx.coroutines.flow.Flow
import space.be1ski.vibits.shared.app.presentation.action.AppAction
import space.be1ski.vibits.shared.app.presentation.effect.AppEffect
import space.be1ski.vibits.shared.core.elm.EffectHandler
import space.be1ski.vibits.shared.core.elm.sideEffect
import space.be1ski.vibits.shared.feature.settings.domain.usecase.SaveTimeRangeTabUseCase
import space.be1ski.vibits.shared.feature.settings.domain.usecase.TimeRangeScreen

internal class AppEffectHandler(
  private val saveTimeRangeTab: SaveTimeRangeTabUseCase,
) : EffectHandler<AppEffect, AppAction> {
  override fun invoke(effect: AppEffect): Flow<AppAction> =
    sideEffect {
      when (effect) {
        is AppEffect.SaveHabitsTimeRangeTab -> {
          saveTimeRangeTab(TimeRangeScreen.HABITS, effect.tab)
        }
        is AppEffect.SavePostsTimeRangeTab -> {
          saveTimeRangeTab(TimeRangeScreen.POSTS, effect.tab)
        }
      }
    }
}
