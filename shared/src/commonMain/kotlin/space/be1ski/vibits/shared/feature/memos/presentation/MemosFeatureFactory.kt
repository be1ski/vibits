package space.be1ski.vibits.shared.feature.memos.presentation

import space.be1ski.vibits.shared.core.elm.Feature
import space.be1ski.vibits.shared.core.elm.FeatureImpl
import space.be1ski.vibits.shared.feature.memos.di.MemosDependencies

fun createMemosFeature(
  dependencies: MemosDependencies,
  isOfflineMode: Boolean = false,
  initialState: MemosState = MemosState(),
): Feature<MemosAction, MemosState, MemosEffect> {
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
        loadCredentials = dependencies.loadCredentials,
        saveCredentials = dependencies.saveCredentials,
        loadMemos = dependencies.loadMemos,
        loadCachedMemos = dependencies.loadCachedMemos,
        createMemo = dependencies.createMemo,
        updateMemo = dependencies.updateMemo,
        deleteMemo = dependencies.deleteMemo,
      ),
    initialEffects = if (!needsCredentials) listOf(MemosEffect.LoadCachedMemos) else emptyList(),
  )
}
