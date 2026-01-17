package space.be1ski.vibits.shared.feature.memos.domain.usecase

import space.be1ski.vibits.shared.feature.memos.domain.model.Memo
import space.be1ski.vibits.shared.feature.memos.domain.repository.MemosRepository

class UpdateMemoUseCase(
  private val memosRepository: MemosRepository,
) {
  suspend operator fun invoke(
    name: String,
    content: String,
  ): Memo = memosRepository.updateMemo(name, content)
}
