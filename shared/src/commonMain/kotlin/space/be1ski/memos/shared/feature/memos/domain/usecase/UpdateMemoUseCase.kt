package space.be1ski.memos.shared.feature.memos.domain.usecase

import space.be1ski.memos.shared.feature.memos.domain.model.Memo
import space.be1ski.memos.shared.feature.memos.domain.repository.MemosRepository

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
