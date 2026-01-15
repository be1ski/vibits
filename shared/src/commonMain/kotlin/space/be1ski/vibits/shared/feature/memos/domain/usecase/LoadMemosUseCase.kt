package space.be1ski.vibits.shared.feature.memos.domain.usecase

import space.be1ski.vibits.shared.feature.memos.domain.model.Memo
import space.be1ski.vibits.shared.feature.memos.domain.repository.MemosRepository

class LoadMemosUseCase(
  private val memosRepository: MemosRepository
) {
  suspend operator fun invoke(): List<Memo> = memosRepository.listMemos()
}
