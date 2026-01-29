package space.be1ski.vibits.shared.feature.memos.di

import space.be1ski.vibits.shared.core.elm.Feature
import space.be1ski.vibits.shared.core.elm.FeatureImpl
import space.be1ski.vibits.shared.feature.memos.presentation.action.MemosAction
import space.be1ski.vibits.shared.feature.memos.presentation.effect.MemosCredentialsEffectHandler
import space.be1ski.vibits.shared.feature.memos.presentation.effect.MemosEffect
import space.be1ski.vibits.shared.feature.memos.presentation.effect.MemosEffectHandler
import space.be1ski.vibits.shared.feature.memos.presentation.effect.MemosLoadEffectHandler
import space.be1ski.vibits.shared.feature.memos.presentation.effect.MemosWriteEffectHandler
import space.be1ski.vibits.shared.feature.memos.presentation.reducer.memosReducer
import space.be1ski.vibits.shared.feature.memos.presentation.state.MemosState

fun createMemosFeature(
  dependencies: MemosDependencies,
  isOfflineMode: Boolean = false,
  initialState: MemosState = MemosState(),
): Feature<MemosAction, MemosState, MemosEffect, Nothing> {
  val creds = dependencies.loadCredentials()
  val needsCredentials = !isOfflineMode && (creds.baseUrl.isBlank() || creds.token.isBlank())

  return FeatureImpl(
    initialState =
      initialState.copy(
        baseUrl = creds.baseUrl,
        token = creds.token,
        credentialsMode = needsCredentials,
        isOfflineMode = isOfflineMode,
      ),
    reducer = memosReducer,
    effectHandler =
      MemosEffectHandler(
        credentialsHandler =
          MemosCredentialsEffectHandler(
            loadCredentials = dependencies.loadCredentials,
            saveCredentials = dependencies.saveCredentials,
          ),
        loadHandler =
          MemosLoadEffectHandler(
            loadMemos = dependencies.loadMemos,
            loadCachedMemos = dependencies.loadCachedMemos,
          ),
        writeHandler =
          MemosWriteEffectHandler(
            createMemo = dependencies.createMemo,
            updateMemo = dependencies.updateMemo,
            deleteMemo = dependencies.deleteMemo,
          ),
      ),
    initialCommands = if (!needsCredentials) listOf(MemosEffect.LoadCachedMemos) else emptyList(),
  )
}
