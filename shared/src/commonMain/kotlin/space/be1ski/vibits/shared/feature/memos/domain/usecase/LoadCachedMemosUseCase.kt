package space.be1ski.vibits.shared.feature.memos.domain.usecase

import javax.inject.Inject
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo
import space.be1ski.vibits.shared.feature.memos.domain.repository.MemosRepository

class LoadCachedMemosUseCase @Inject constructor(
  private val memosRepository: MemosRepository,
) {
  suspend operator fun invoke(): List<Memo> = memosRepository.cachedMemos()
}
