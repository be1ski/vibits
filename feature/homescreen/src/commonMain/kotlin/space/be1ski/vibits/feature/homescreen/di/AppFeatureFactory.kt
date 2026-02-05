package space.be1ski.vibits.feature.homescreen.di

import kotlinx.datetime.LocalDate
import space.be1ski.vibits.core.elm.Feature
import space.be1ski.vibits.core.elm.FeatureImpl
import space.be1ski.vibits.core.platform.mode.AppMode
import space.be1ski.vibits.feature.homescreen.domain.model.AppState
import space.be1ski.vibits.feature.homescreen.presentation.action.AppAction
import space.be1ski.vibits.feature.homescreen.presentation.effect.AppEffect
import space.be1ski.vibits.feature.homescreen.presentation.effect.AppEffectHandler
import space.be1ski.vibits.feature.homescreen.presentation.reducer.appReducer
import space.be1ski.vibits.feature.settings.domain.model.TimeRangeTab
import space.be1ski.vibits.feature.settings.domain.usecase.SaveTimeRangeTabUseCase

internal fun createAppFeature(
  saveTimeRangeTab: SaveTimeRangeTabUseCase,
  initialMode: AppMode,
  currentDate: LocalDate,
  initialHabitsTab: TimeRangeTab = TimeRangeTab.WEEKS,
  initialPostsTab: TimeRangeTab = TimeRangeTab.WEEKS,
): Feature<AppAction, AppState, AppEffect, Nothing> =
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
