package space.be1ski.vibits.shared.feature.habits.presentation

import space.be1ski.vibits.shared.core.elm.Reducer
import space.be1ski.vibits.shared.feature.habits.presentation.reducer.cacheReducer
import space.be1ski.vibits.shared.feature.habits.presentation.reducer.configDeleteReducer
import space.be1ski.vibits.shared.feature.habits.presentation.reducer.configReducer
import space.be1ski.vibits.shared.feature.habits.presentation.reducer.configWarningReducer
import space.be1ski.vibits.shared.feature.habits.presentation.reducer.editorReducer
import space.be1ski.vibits.shared.feature.habits.presentation.reducer.responseReducer
import space.be1ski.vibits.shared.feature.habits.presentation.reducer.selectionReducer
import space.be1ski.vibits.shared.feature.habits.presentation.reducer.singleToggleReducer

/**
 * Main reducer that delegates to sub-reducers.
 */
val habitsReducer: Reducer<HabitsAction, HabitsState, HabitsEffect, Nothing> =
  { action, state ->
    when (action) {
      is HabitsAction.Editor -> editorReducer(action, state)
      is HabitsAction.Config -> configReducer(action, state)
      is HabitsAction.ConfigWarning -> configWarningReducer(action, state)
      is HabitsAction.ConfigDelete -> configDeleteReducer(action, state)
      is HabitsAction.SingleToggle -> singleToggleReducer(action, state)
      is HabitsAction.Selection -> selectionReducer(action, state)
      is HabitsAction.Response -> responseReducer(action, state)
      is HabitsAction.Cache -> cacheReducer(action, state)
    }
  }
