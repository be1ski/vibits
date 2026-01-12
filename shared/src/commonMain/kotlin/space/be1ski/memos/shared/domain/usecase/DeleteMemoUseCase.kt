package space.be1ski.memos.shared.domain.usecase

import space.be1ski.memos.shared.domain.repository.MemosRepository

/**
 * Deletes a memo by name.
 */
class DeleteMemoUseCase(
  private val memosRepository: MemosRepository
) {
  /**
   * Deletes memo by name.
   */
  suspend operator fun invoke(name: String) {
    memosRepository.deleteMemo(name)
  }
}
