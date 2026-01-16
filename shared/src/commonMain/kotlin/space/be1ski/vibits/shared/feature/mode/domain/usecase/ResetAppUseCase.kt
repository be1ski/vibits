package space.be1ski.vibits.shared.feature.mode.domain.usecase

import space.be1ski.vibits.shared.feature.auth.domain.model.Credentials
import space.be1ski.vibits.shared.feature.auth.domain.repository.CredentialsRepository
import space.be1ski.vibits.shared.feature.memos.data.local.MemoCache
import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode
import space.be1ski.vibits.shared.feature.mode.domain.repository.AppModeRepository
import space.be1ski.vibits.shared.feature.preferences.domain.model.TimeRangeTab
import space.be1ski.vibits.shared.feature.preferences.domain.model.UserPreferences
import space.be1ski.vibits.shared.feature.preferences.domain.repository.PreferencesRepository

/**
 * Use case for resetting app to initial state.
 * Clears mode selection, cache, credentials, and preferences, showing mode selection screen on next launch.
 */
class ResetAppUseCase(
  private val appModeRepository: AppModeRepository,
  private val memoCache: MemoCache,
  private val credentialsRepository: CredentialsRepository,
  private val preferencesRepository: PreferencesRepository
) {
  suspend operator fun invoke() {
    memoCache.clear()
    credentialsRepository.save(Credentials(baseUrl = "", token = ""))
    preferencesRepository.save(UserPreferences(TimeRangeTab.Weeks, TimeRangeTab.Weeks))
    appModeRepository.saveMode(AppMode.NotSelected)
  }
}
