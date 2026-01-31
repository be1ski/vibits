package space.be1ski.vibits.feature.memos.domain.usecase

import dev.zacsweers.metro.Inject
import space.be1ski.vibits.feature.memos.domain.repository.MemosRepository

@Inject
class DeleteMemoUseCase(
  private val memosRepository: MemosRepository,
) {
  suspend operator fun invoke(name: String) {
    memosRepository.deleteMemo(name)
  }
}
