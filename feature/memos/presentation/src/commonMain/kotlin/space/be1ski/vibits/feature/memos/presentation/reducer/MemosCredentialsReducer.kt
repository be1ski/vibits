package space.be1ski.vibits.feature.memos.presentation.reducer

import space.be1ski.vibits.core.elm.Reducer
import space.be1ski.vibits.core.elm.reducer
import space.be1ski.vibits.feature.memos.presentation.action.MemosAction
import space.be1ski.vibits.feature.memos.presentation.effect.MemosEffect
import space.be1ski.vibits.feature.memos.presentation.state.MemosState

internal val credentialsReducer: Reducer<MemosAction.Credentials, MemosState, MemosEffect, Nothing> =
  reducer { action, state ->
    when (action) {
      is MemosAction.Credentials.UpdateBaseUrl -> {
        state { state.copy(baseUrl = action.value, errorMessage = null) }
      }

      is MemosAction.Credentials.UpdateToken -> {
        state { state.copy(token = action.value, errorMessage = null) }
      }

      is MemosAction.Credentials.EditCredentials -> {
        state { state.copy(credentialsMode = true, errorMessage = null) }
        command(MemosEffect.LoadCredentials)
      }

      is MemosAction.Credentials.CredentialsLoaded -> {
        state { state.copy(baseUrl = action.baseUrl, token = action.token) }
      }
    }
  }
