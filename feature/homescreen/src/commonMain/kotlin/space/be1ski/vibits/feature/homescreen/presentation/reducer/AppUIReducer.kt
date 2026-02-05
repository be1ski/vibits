package space.be1ski.vibits.feature.homescreen.presentation.reducer

import space.be1ski.vibits.core.elm.Reducer
import space.be1ski.vibits.core.elm.reducer
import space.be1ski.vibits.feature.homescreen.domain.model.AppState
import space.be1ski.vibits.feature.homescreen.presentation.action.AppAction
import space.be1ski.vibits.feature.homescreen.presentation.effect.AppEffect

internal val uiReducer: Reducer<AppAction.UI, AppState, AppEffect, Nothing> =
  reducer { action, state ->
    when (action) {
      is AppAction.UI.MarkAutoLoaded -> {
        state { state.copy(autoLoaded = true) }
      }

      is AppAction.UI.SetPostsListExpanded -> {
        state { state.copy(postsListExpanded = action.expanded) }
      }
    }
  }
