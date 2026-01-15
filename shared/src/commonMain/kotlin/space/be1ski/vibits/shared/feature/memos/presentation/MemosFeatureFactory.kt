package space.be1ski.vibits.shared.feature.memos.presentation

import space.be1ski.vibits.shared.core.elm.Feature
import space.be1ski.vibits.shared.core.elm.FeatureImpl

fun createMemosFeature(
  useCases: MemosUseCases,
  isOfflineMode: Boolean = false,
  initialState: MemosState = MemosState()
): Feature<MemosAction, MemosState, MemosEffect> {
  val creds = useCases.loadCredentials()
  val needsCredentials = !isOfflineMode && (creds.baseUrl.isBlank() || creds.token.isBlank())

  return FeatureImpl(
    initialState = initialState.copy(
      baseUrl = creds.baseUrl,
      token = creds.token,
      credentialsMode = needsCredentials,
      isOfflineMode = isOfflineMode
    ),
    reducer = memosReducer,
    effectHandler = MemosEffectHandler(useCases),
    initialEffects = if (!needsCredentials) listOf(MemosEffect.LoadCachedMemos) else emptyList()
  )
}
