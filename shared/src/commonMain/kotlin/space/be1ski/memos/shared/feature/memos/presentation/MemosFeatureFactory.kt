package space.be1ski.memos.shared.feature.memos.presentation

import space.be1ski.memos.shared.core.elm.Feature
import space.be1ski.memos.shared.core.elm.FeatureImpl

fun createMemosFeature(
  useCases: MemosUseCases,
  initialState: MemosState = MemosState()
): Feature<MemosAction, MemosState, MemosEffect> {
  val creds = useCases.loadCredentials()
  val needsCredentials = creds.baseUrl.isBlank() || creds.token.isBlank()

  return FeatureImpl(
    initialState = initialState.copy(
      baseUrl = creds.baseUrl,
      token = creds.token,
      credentialsMode = needsCredentials
    ),
    reducer = memosReducer,
    effectHandler = MemosEffectHandler(useCases),
    initialEffects = if (!needsCredentials) listOf(MemosEffect.LoadCachedMemos) else emptyList()
  )
}
