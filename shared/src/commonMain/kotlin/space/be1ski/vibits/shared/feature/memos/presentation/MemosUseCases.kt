package space.be1ski.vibits.shared.feature.memos.presentation

import space.be1ski.vibits.shared.feature.auth.domain.usecase.LoadCredentialsUseCase
import space.be1ski.vibits.shared.feature.auth.domain.usecase.SaveCredentialsUseCase
import space.be1ski.vibits.shared.feature.memos.domain.usecase.CreateMemoUseCase
import space.be1ski.vibits.shared.feature.memos.domain.usecase.DeleteMemoUseCase
import space.be1ski.vibits.shared.feature.memos.domain.usecase.LoadCachedMemosUseCase
import space.be1ski.vibits.shared.feature.memos.domain.usecase.LoadMemosUseCase
import space.be1ski.vibits.shared.feature.memos.domain.usecase.UpdateMemoUseCase

data class MemosUseCases(
  val loadMemos: LoadMemosUseCase,
  val loadCachedMemos: LoadCachedMemosUseCase,
  val loadCredentials: LoadCredentialsUseCase,
  val saveCredentials: SaveCredentialsUseCase,
  val createMemo: CreateMemoUseCase,
  val updateMemo: UpdateMemoUseCase,
  val deleteMemo: DeleteMemoUseCase,
)
