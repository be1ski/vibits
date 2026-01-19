package space.be1ski.vibits.shared.app.di

import kotlinx.datetime.LocalDate
import space.be1ski.vibits.shared.app.presentation.AppAction
import space.be1ski.vibits.shared.app.presentation.AppEffect
import space.be1ski.vibits.shared.app.presentation.AppEffectHandler
import space.be1ski.vibits.shared.app.presentation.AppState
import space.be1ski.vibits.shared.app.presentation.appReducer
import space.be1ski.vibits.shared.core.elm.Feature
import space.be1ski.vibits.shared.core.elm.FeatureImpl
import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode
import space.be1ski.vibits.shared.feature.settings.domain.model.TimeRangeTab
import space.be1ski.vibits.shared.feature.settings.domain.usecase.SaveTimeRangeTabUseCase

internal fun createAppFeature(
  saveTimeRangeTab: SaveTimeRangeTabUseCase,
  initialMode: AppMode,
  currentDate: LocalDate,
  initialHabitsTab: TimeRangeTab = TimeRangeTab.WEEKS,
  initialPostsTab: TimeRangeTab = TimeRangeTab.WEEKS,
): Feature<AppAction, AppState, AppEffect> =
  FeatureImpl(
    initialState =
      AppState(
        appMode = initialMode,
        periodStartDate = currentDate,
        habitsTimeRangeTab = initialHabitsTab,
        postsTimeRangeTab = initialPostsTab,
      ),
    reducer = appReducer,
    effectHandler = AppEffectHandler(saveTimeRangeTab),
  )
