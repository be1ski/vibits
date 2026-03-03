package space.be1ski.vibits.feature.mode.presentation.reducer

import space.be1ski.vibits.core.elm.Reducer
import space.be1ski.vibits.core.elm.reducer
import space.be1ski.vibits.feature.mode.presentation.action.ModeSelectionAction
import space.be1ski.vibits.feature.mode.presentation.effect.ModeSelectionEffect.Command
import space.be1ski.vibits.feature.mode.presentation.effect.ModeSelectionEffect.Notification
import space.be1ski.vibits.feature.mode.presentation.state.ModeSelectionState

internal val storedCredentialsReducer:
  Reducer<ModeSelectionAction.StoredCredentials, ModeSelectionState, Command, Notification> =
  reducer { action, state ->
    when (action) {
      is ModeSelectionAction.StoredCredentials.Found -> {
        state {
          state.copy(
            hasStoredCredentials = true,
            showQuickOnlineDialog = true,
            isKeychainAvailable = action.isKeychainAvailable,
          )
        }
      }

      is ModeSelectionAction.StoredCredentials.NotFound -> {
        state {
          state.copy(
            hasStoredCredentials = false,
            isKeychainAvailable = action.isKeychainAvailable,
          )
        }
      }
    }
  }
