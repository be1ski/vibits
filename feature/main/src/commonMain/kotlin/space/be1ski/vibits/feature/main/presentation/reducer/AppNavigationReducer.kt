package space.be1ski.vibits.feature.main.presentation.reducer

import space.be1ski.vibits.core.elm.Reducer
import space.be1ski.vibits.core.elm.reducer
import space.be1ski.vibits.feature.main.domain.model.AppState
import space.be1ski.vibits.feature.main.presentation.action.AppAction
import space.be1ski.vibits.feature.main.presentation.effect.AppEffect

internal val navigationReducer: Reducer<AppAction.Navigation, AppState, AppEffect, Nothing> =
  reducer { action, state ->
    when (action) {
      is AppAction.Navigation.SelectScreen -> {
        state { state.copy(selectedScreen = action.screen) }
      }
    }
  }
