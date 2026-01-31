package space.be1ski.vibits.feature.memos.domain.usecase

import dev.zacsweers.metro.Inject
import space.be1ski.vibits.feature.memos.domain.model.Memo
import space.be1ski.vibits.feature.memos.domain.repository.MemosRepository

@Inject
class UpdateMemoUseCase(
  private val memosRepository: MemosRepository,
) {
  suspend operator fun invoke(
    name: String,
    content: String,
  ): Memo = memosRepository.updateMemo(name, content)
}
