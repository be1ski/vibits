package space.be1ski.vibits.feature.changelog.domain.usecase

import dev.zacsweers.metro.Inject
import space.be1ski.vibits.core.utils.coroutines.runSuspendCatching
import space.be1ski.vibits.core.utils.logging.Log
import space.be1ski.vibits.feature.changelog.domain.model.UpdateAvailability
import space.be1ski.vibits.feature.changelog.domain.model.compareVersions
import space.be1ski.vibits.feature.changelog.domain.model.parseVersion
import space.be1ski.vibits.feature.changelog.domain.repository.ChangelogRepository
import space.be1ski.vibits.feature.changelog.domain.repository.InstallationSource

private const val TAG = "UpdateCheck"
private const val VERSION_PAD_LENGTH = 10

@Inject
class CheckForUpdateUseCase(
  private val changelogRepository: ChangelogRepository,
  private val installationSource: InstallationSource,
) {
  suspend operator fun invoke(currentVersion: String): UpdateAvailability? {
    if (currentVersion == "dev" || currentVersion == "web") {
      Log.d(TAG, "Skipping update check for version: $currentVersion")
      return null
    }
    return parseVersion(currentVersion)?.let { currentParsed ->
      fetchLatestRelease()?.toUpdateAvailability(currentVersion, currentParsed)
    }
  }

  private fun Pair<String, List<Int>>.toUpdateAvailability(
    currentVersion: String,
    currentParsed: List<Int>,
  ): UpdateAvailability? {
    val (latestVersion, latestParsed) = this
    if (compareVersions(latestParsed, currentParsed) <= 0) return null
    val cleanLatest = latestVersion.removePrefix("v")
    Log.i(TAG, "Update available: $currentVersion → $cleanLatest")
    return UpdateAvailability(
      latestVersion = cleanLatest,
      currentVersion = currentVersion,
      isHomebrewInstallation = installationSource.isHomebrew(),
    )
  }

  private suspend fun fetchLatestRelease(): Pair<String, List<Int>>? {
    val releases =
      runSuspendCatching { changelogRepository.getReleases() }
        .onFailure { Log.e(TAG, "Failed to fetch releases", it) }
        .getOrNull() ?: return null

    return releases
      .mapNotNull { entry ->
        parseVersion(entry.version)?.let { parsed -> entry.version to parsed }
      }.maxByOrNull { (_, parsed) ->
        parsed.joinToString(".") { it.toString().padStart(VERSION_PAD_LENGTH, '0') }
      }
  }
}
