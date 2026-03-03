package space.be1ski.vibits.feature.mode.presentation.reducer

import space.be1ski.vibits.core.elm.Reducer
import space.be1ski.vibits.feature.mode.presentation.action.ModeSelectionAction
import space.be1ski.vibits.feature.mode.presentation.effect.ModeSelectionEffect
import space.be1ski.vibits.feature.mode.presentation.state.ModeSelectionState

val modeSelectionReducer: Reducer<ModeSelectionAction, ModeSelectionState, ModeSelectionEffect.Command, ModeSelectionEffect.Notification> =
  { action, state ->
    when (action) {
      is ModeSelectionAction.StoredCredentials -> storedCredentialsReducer(action, state)
      is ModeSelectionAction.QuickOnline -> quickOnlineReducer(action, state)
      is ModeSelectionAction.Dialog -> dialogReducer(action, state)
      is ModeSelectionAction.Input -> inputReducer(action, state)
      is ModeSelectionAction.Validation -> validationReducer(action, state)
      is ModeSelectionAction.Selection -> selectionReducer(action, state)
      is ModeSelectionAction.Keychain -> keychainReducer(action, state)
    }
  }
