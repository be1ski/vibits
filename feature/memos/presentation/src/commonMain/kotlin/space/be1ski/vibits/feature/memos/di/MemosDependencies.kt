package space.be1ski.vibits.feature.memos.di

import dev.zacsweers.metro.Inject
import space.be1ski.vibits.feature.auth.domain.usecase.LoadCredentialsUseCase
import space.be1ski.vibits.feature.auth.domain.usecase.SaveCredentialsUseCase
import space.be1ski.vibits.feature.memos.domain.usecase.CreateMemoUseCase
import space.be1ski.vibits.feature.memos.domain.usecase.DeleteMemoUseCase
import space.be1ski.vibits.feature.memos.domain.usecase.LoadCachedMemosUseCase
import space.be1ski.vibits.feature.memos.domain.usecase.LoadMemosUseCase
import space.be1ski.vibits.feature.memos.domain.usecase.UpdateMemoUseCase
import space.be1ski.vibits.feature.settings.domain.usecase.LoadSyncDebounceDurationUseCase
import space.be1ski.vibits.feature.sync.domain.SyncEngine
import space.be1ski.vibits.feature.sync.domain.repository.SyncQueueRepository

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
  val loadSyncDebounceDuration: LoadSyncDebounceDurationUseCase,
  val syncEngine: SyncEngine,
  val syncQueueRepository: SyncQueueRepository,
)
