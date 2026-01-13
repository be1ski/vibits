package space.be1ski.memos.shared.domain.usecase

import space.be1ski.memos.shared.domain.model.memo.Memo
import space.be1ski.memos.shared.domain.repository.MemosRepository

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
