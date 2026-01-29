package space.be1ski.vibits.shared.feature.memos.di

import space.be1ski.vibits.shared.core.elm.Feature
import space.be1ski.vibits.shared.core.elm.FeatureImpl
import space.be1ski.vibits.shared.feature.memos.presentation.MemosAction
import space.be1ski.vibits.shared.feature.memos.presentation.MemosEffect
import space.be1ski.vibits.shared.feature.memos.presentation.MemosState
import space.be1ski.vibits.shared.feature.memos.presentation.handler.MemosCredentialsEffectHandler
import space.be1ski.vibits.shared.feature.memos.presentation.handler.MemosEffectHandler
import space.be1ski.vibits.shared.feature.memos.presentation.handler.MemosLoadEffectHandler
import space.be1ski.vibits.shared.feature.memos.presentation.handler.MemosWriteEffectHandler
import space.be1ski.vibits.shared.feature.memos.presentation.memosReducer

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
