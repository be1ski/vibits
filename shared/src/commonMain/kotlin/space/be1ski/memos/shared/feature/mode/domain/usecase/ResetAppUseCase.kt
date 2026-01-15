package space.be1ski.memos.shared.feature.mode.domain.usecase

import space.be1ski.memos.shared.feature.auth.domain.model.Credentials
import space.be1ski.memos.shared.feature.auth.domain.repository.CredentialsRepository
import space.be1ski.memos.shared.feature.memos.data.local.MemoCache
import space.be1ski.memos.shared.feature.mode.domain.model.AppMode
import space.be1ski.memos.shared.feature.mode.domain.repository.AppModeRepository

/**
 * Use case for resetting app to initial state.
 * Clears mode selection, cache, and credentials, showing mode selection screen on next launch.
 */
class ResetAppUseCase(
  private val appModeRepository: AppModeRepository,
  private val memoCache: MemoCache,
  private val credentialsRepository: CredentialsRepository
) {
  suspend operator fun invoke() {
    memoCache.clear()
    credentialsRepository.save(Credentials(baseUrl = "", token = ""))
    appModeRepository.saveMode(AppMode.NotSelected)
  }
}
