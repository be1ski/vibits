package space.be1ski.memos.shared.domain.usecase

import space.be1ski.memos.shared.domain.model.Memo
import space.be1ski.memos.shared.domain.repository.MemosRepository

/**
 * Updates memo content in the repository.
 */
class UpdateMemoUseCase(
  private val memosRepository: MemosRepository
) {
  /**
   * Updates memo content and returns the updated memo.
   */
  suspend operator fun invoke(name: String, content: String): Memo =
    memosRepository.updateMemo(name, content)
}
