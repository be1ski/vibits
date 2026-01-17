package space.be1ski.vibits.shared.feature.memos.domain.usecase

import dev.zacsweers.metro.Inject
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo
import space.be1ski.vibits.shared.feature.memos.domain.repository.MemosRepository

@Inject
class LoadCachedMemosUseCase(
  private val memosRepository: MemosRepository,
) {
  suspend operator fun invoke(): List<Memo> = memosRepository.cachedMemos()
}
