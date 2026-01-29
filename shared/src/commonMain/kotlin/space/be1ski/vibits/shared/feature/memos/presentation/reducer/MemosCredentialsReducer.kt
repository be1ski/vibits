package space.be1ski.vibits.shared.feature.memos.presentation.reducer

import space.be1ski.vibits.shared.core.elm.Reducer
import space.be1ski.vibits.shared.core.elm.reducer
import space.be1ski.vibits.shared.feature.memos.presentation.action.MemosAction
import space.be1ski.vibits.shared.feature.memos.presentation.effect.MemosEffect
import space.be1ski.vibits.shared.feature.memos.presentation.state.MemosState

internal val credentialsReducer: Reducer<MemosAction.Credentials, MemosState, MemosEffect, Nothing> =
  reducer { action, state ->
    when (action) {
      is MemosAction.Credentials.UpdateBaseUrl -> {
        state { copy(baseUrl = action.value, errorMessage = null) }
      }

      is MemosAction.Credentials.UpdateToken -> {
        state { copy(token = action.value, errorMessage = null) }
      }

      is MemosAction.Credentials.EditCredentials -> {
        state { copy(credentialsMode = true, errorMessage = null) }
        command(MemosEffect.LoadCredentials)
      }

      is MemosAction.Credentials.CredentialsLoaded -> {
        state { copy(baseUrl = action.baseUrl, token = action.token) }
      }
    }
  }
