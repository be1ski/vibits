package space.be1ski.vibits.feature.homescreen.presentation.reducer

import space.be1ski.vibits.core.elm.Reducer
import space.be1ski.vibits.feature.homescreen.domain.model.AppState
import space.be1ski.vibits.feature.homescreen.presentation.action.AppAction
import space.be1ski.vibits.feature.homescreen.presentation.effect.AppEffect

/**
 * Pure reducer for the App coordinator feature.
 * Delegates to sub-reducers based on action type.
 */
internal val appReducer: Reducer<AppAction, AppState, AppEffect, Nothing> =
  { action, state ->
    when (action) {
      is AppAction.Navigation -> navigationReducer(action, state)
      is AppAction.TimeRange -> timeRangeReducer(action, state)
      is AppAction.Mode -> modeReducer(action, state)
      is AppAction.UI -> uiReducer(action, state)
    }
  }
