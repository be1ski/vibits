package space.be1ski.vibits.feature.settings.domain.usecase

import dev.zacsweers.metro.Inject
import space.be1ski.vibits.feature.settings.domain.repository.PreferencesRepository
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@Inject
class SaveSyncDebounceUseCase(
  private val preferencesRepository: PreferencesRepository,
) {
  operator fun invoke(seconds: Int) {
    val duration: Duration = seconds.seconds
    val currentPrefs = preferencesRepository.load()
    val updatedPrefs = currentPrefs.copy(memosAutoSyncDebounceDuration = duration)
    preferencesRepository.save(updatedPrefs)
  }
}
