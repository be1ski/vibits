package space.be1ski.memos.shared.feature.memos.domain.usecase

import space.be1ski.memos.shared.feature.memos.domain.model.Memo
import space.be1ski.memos.shared.feature.memos.domain.repository.MemosRepository

/**
 * Loads cached memos from local storage.
 */
class LoadCachedMemosUseCase(
  private val memosRepository: MemosRepository
) {
  /**
   * Loads memos stored in the local cache.
   */
  suspend operator fun invoke(): List<Memo> = memosRepository.cachedMemos()
}
