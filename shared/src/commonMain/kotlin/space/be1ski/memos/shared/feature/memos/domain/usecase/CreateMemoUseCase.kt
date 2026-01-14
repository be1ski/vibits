package space.be1ski.memos.shared.feature.memos.domain.usecase

import space.be1ski.memos.shared.feature.memos.domain.model.Memo
import space.be1ski.memos.shared.feature.memos.domain.repository.MemosRepository

/**
 * Creates a new memo in the repository.
 */
class CreateMemoUseCase(
  private val memosRepository: MemosRepository
) {
  /**
   * Creates a memo and returns it.
   */
  suspend operator fun invoke(content: String): Memo = memosRepository.createMemo(content)
}
