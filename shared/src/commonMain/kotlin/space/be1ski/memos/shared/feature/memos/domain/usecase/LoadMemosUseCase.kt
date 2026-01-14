package space.be1ski.memos.shared.feature.memos.domain.usecase

import space.be1ski.memos.shared.feature.memos.domain.model.Memo
import space.be1ski.memos.shared.feature.memos.domain.repository.MemosRepository

/**
 * Loads memos using the domain repository.
 */
class LoadMemosUseCase(
  private val memosRepository: MemosRepository
) {
  /**
   * Loads all memos for the given credentials.
   */
  suspend operator fun invoke(): List<Memo> = memosRepository.listMemos()
}
