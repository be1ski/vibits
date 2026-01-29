package space.be1ski.vibits.shared.feature.memos.presentation.reducer

import space.be1ski.vibits.shared.core.elm.ReducerResult
import space.be1ski.vibits.shared.core.elm.reducer
import space.be1ski.vibits.shared.feature.memos.presentation.action.MemosAction
import space.be1ski.vibits.shared.feature.memos.presentation.effect.MemosEffect
import space.be1ski.vibits.shared.feature.memos.presentation.state.MemosState

internal fun credentialsReducer(
  action: MemosAction.Credentials,
  state: MemosState,
): ReducerResult<MemosState, MemosEffect, Nothing> =
  reducer<MemosAction.Credentials, MemosState, MemosEffect, Nothing> { a, s ->
    when (a) {
      is MemosAction.Credentials.UpdateBaseUrl -> {
        state { copy(baseUrl = a.value, errorMessage = null) }
      }

      is MemosAction.Credentials.UpdateToken -> {
        state { copy(token = a.value, errorMessage = null) }
      }

      is MemosAction.Credentials.EditCredentials -> {
        state { copy(credentialsMode = true, errorMessage = null) }
        command(MemosEffect.LoadCredentials)
      }

      is MemosAction.Credentials.CredentialsLoaded -> {
        state { copy(baseUrl = a.baseUrl, token = a.token) }
      }
    }
  }(action, state)
