package space.be1ski.vibits.shared.feature.memos.di

import dev.zacsweers.metro.Inject
import space.be1ski.vibits.shared.feature.auth.domain.usecase.LoadCredentialsUseCase
import space.be1ski.vibits.shared.feature.auth.domain.usecase.SaveCredentialsUseCase
import space.be1ski.vibits.shared.feature.memos.domain.usecase.CreateMemoUseCase
import space.be1ski.vibits.shared.feature.memos.domain.usecase.DeleteMemoUseCase
import space.be1ski.vibits.shared.feature.memos.domain.usecase.LoadCachedMemosUseCase
import space.be1ski.vibits.shared.feature.memos.domain.usecase.LoadMemosUseCase
import space.be1ski.vibits.shared.feature.memos.domain.usecase.UpdateMemoUseCase
import space.be1ski.vibits.shared.feature.sync.domain.repository.SyncQueueRepository
import space.be1ski.vibits.shared.feature.sync.domain.usecase.SyncEngine

@Suppress("LongParameterList")
@Inject
class MemosDependencies(
  val loadMemos: LoadMemosUseCase,
  val loadCachedMemos: LoadCachedMemosUseCase,
  val loadCredentials: LoadCredentialsUseCase,
  val saveCredentials: SaveCredentialsUseCase,
  val createMemo: CreateMemoUseCase,
  val updateMemo: UpdateMemoUseCase,
  val deleteMemo: DeleteMemoUseCase,
  val syncEngine: SyncEngine,
  val syncQueueRepository: SyncQueueRepository,
)
