package space.be1ski.memos.shared.presentation.memos

import space.be1ski.memos.shared.domain.usecase.CreateMemoUseCase
import space.be1ski.memos.shared.domain.usecase.DeleteMemoUseCase
import space.be1ski.memos.shared.domain.usecase.LoadCachedMemosUseCase
import space.be1ski.memos.shared.domain.usecase.LoadCredentialsUseCase
import space.be1ski.memos.shared.domain.usecase.LoadMemosUseCase
import space.be1ski.memos.shared.domain.usecase.SaveCredentialsUseCase
import space.be1ski.memos.shared.domain.usecase.UpdateMemoUseCase
import space.be1ski.memos.shared.elm.Feature
import space.be1ski.memos.shared.elm.FeatureImpl

/**
 * Creates a new MemosFeature instance.
 */
fun createMemosFeature(
  loadMemosUseCase: LoadMemosUseCase,
  loadCachedMemosUseCase: LoadCachedMemosUseCase,
  loadCredentialsUseCase: LoadCredentialsUseCase,
  saveCredentialsUseCase: SaveCredentialsUseCase,
  createMemoUseCase: CreateMemoUseCase,
  updateMemoUseCase: UpdateMemoUseCase,
  deleteMemoUseCase: DeleteMemoUseCase,
  initialState: MemosState = MemosState()
): Feature<MemosAction, MemosState, MemosEffect> {
  val creds = loadCredentialsUseCase()
  val needsCredentials = creds.baseUrl.isBlank() || creds.token.isBlank()

  return FeatureImpl(
    initialState = initialState.copy(
      baseUrl = creds.baseUrl,
      token = creds.token,
      credentialsMode = needsCredentials
    ),
    reducer = memosReducer,
    effectHandler = MemosEffectHandler(
      loadMemosUseCase = loadMemosUseCase,
      loadCachedMemosUseCase = loadCachedMemosUseCase,
      loadCredentialsUseCase = loadCredentialsUseCase,
      saveCredentialsUseCase = saveCredentialsUseCase,
      createMemoUseCase = createMemoUseCase,
      updateMemoUseCase = updateMemoUseCase,
      deleteMemoUseCase = deleteMemoUseCase
    ),
    initialEffects = if (!needsCredentials) listOf(MemosEffect.LoadCachedMemos) else emptyList()
  )
}
