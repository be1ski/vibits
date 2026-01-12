package space.be1ski.memos.shared.domain.usecase

import space.be1ski.memos.shared.domain.model.Memo
import space.be1ski.memos.shared.domain.repository.MemosRepository

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
