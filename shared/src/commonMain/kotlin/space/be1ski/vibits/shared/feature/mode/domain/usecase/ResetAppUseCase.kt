package space.be1ski.vibits.shared.feature.mode.domain.usecase

import javax.inject.Inject
import space.be1ski.vibits.shared.feature.auth.domain.model.Credentials
import space.be1ski.vibits.shared.feature.auth.domain.repository.CredentialsRepository
import space.be1ski.vibits.shared.feature.memos.data.demo.DemoMemosRepository
import space.be1ski.vibits.shared.feature.memos.data.local.MemoCache
import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode
import space.be1ski.vibits.shared.feature.mode.domain.repository.AppModeRepository
import space.be1ski.vibits.shared.feature.settings.domain.model.TimeRangeTab
import space.be1ski.vibits.shared.feature.settings.domain.model.UserPreferences
import space.be1ski.vibits.shared.feature.settings.domain.repository.PreferencesRepository

/**
 * Use case for resetting app to initial state.
 * Clears mode selection, cache, credentials, preferences, and demo data,
 * showing mode selection screen on next launch.
 */
class ResetAppUseCase @Inject constructor(
  private val appModeRepository: AppModeRepository,
  private val memoCache: MemoCache,
  private val credentialsRepository: CredentialsRepository,
  private val preferencesRepository: PreferencesRepository,
  private val demoMemosRepository: DemoMemosRepository,
) {
  suspend operator fun invoke() {
    memoCache.clear()
    credentialsRepository.save(Credentials(baseUrl = "", token = ""))
    preferencesRepository.save(UserPreferences(TimeRangeTab.WEEKS, TimeRangeTab.WEEKS))
    demoMemosRepository.reset()
    appModeRepository.saveMode(AppMode.NOT_SELECTED)
  }
}
