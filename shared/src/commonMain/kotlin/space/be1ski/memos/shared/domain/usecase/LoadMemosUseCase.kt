package space.be1ski.memos.shared.domain.usecase

import space.be1ski.memos.shared.domain.config.MemosDefaults
import space.be1ski.memos.shared.domain.model.Memo
import space.be1ski.memos.shared.domain.repository.MemosRepository

/**
 * Loads memos using the domain repository.
 */
class LoadMemosUseCase(
  private val memosRepository: MemosRepository
) {
  /**
   * Loads all memos for the given credentials.
   */
  suspend operator fun invoke(
    baseUrl: String,
    token: String,
    pageSize: Int = MemosDefaults.DEFAULT_PAGE_SIZE
  ): List<Memo> = memosRepository.listMemos(baseUrl, token, pageSize)
}
