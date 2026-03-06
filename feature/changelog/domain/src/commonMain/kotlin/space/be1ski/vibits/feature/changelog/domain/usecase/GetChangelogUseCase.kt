package space.be1ski.vibits.feature.changelog.domain.usecase

import dev.zacsweers.metro.Inject
import space.be1ski.vibits.core.utils.coroutines.runSuspendCatching
import space.be1ski.vibits.core.utils.logging.Log
import space.be1ski.vibits.feature.changelog.domain.model.ChangelogEntry
import space.be1ski.vibits.feature.changelog.domain.model.compareVersions
import space.be1ski.vibits.feature.changelog.domain.model.parseVersion
import space.be1ski.vibits.feature.changelog.domain.repository.ChangelogRepository
import space.be1ski.vibits.feature.changelog.domain.repository.LastSeenVersionStore

private const val TAG = "Changelog"

@Inject
class GetChangelogUseCase(
  private val changelogRepository: ChangelogRepository,
  private val lastSeenVersionStore: LastSeenVersionStore,
) {
  suspend operator fun invoke(currentVersion: String): List<ChangelogEntry> {
    val lastSeen = resolveLastSeenVersion(currentVersion) ?: return emptyList()
    return fetchAndFilter(lastSeen, currentVersion)
  }

  private fun resolveLastSeenVersion(currentVersion: String): String? =
    when {
      currentVersion == "web" || currentVersion == "dev" -> {
        Log.d(TAG, "Skipping changelog for version: $currentVersion")
        null
      }
      lastSeenVersionStore.getLastSeenVersion() == null -> {
        Log.d(TAG, "First install, saving current version: $currentVersion")
        lastSeenVersionStore.setLastSeenVersion(currentVersion)
        null
      }
      lastSeenVersionStore.getLastSeenVersion() == currentVersion -> null
      else -> lastSeenVersionStore.getLastSeenVersion()
    }

  private suspend fun fetchAndFilter(
    lastSeen: String,
    currentVersion: String,
  ): List<ChangelogEntry> {
    val releases =
      runSuspendCatching { changelogRepository.getReleases() }
        .onFailure { Log.e(TAG, "Failed to fetch releases", it) }
        .getOrNull()

    val lastSeenParsed = releases?.let { parseVersion(lastSeen) }
    val currentParsed = releases?.let { parseVersion(currentVersion) }

    val filtered =
      when {
        releases == null -> emptyList()
        lastSeenParsed == null || currentParsed == null -> {
          Log.w(TAG, "Cannot parse versions: lastSeen=$lastSeen, current=$currentVersion")
          emptyList()
        }
        else ->
          releases
            .filter { entry ->
              val v = parseVersion(entry.version)
              v != null && compareVersions(v, lastSeenParsed) > 0 && compareVersions(v, currentParsed) <= 0
            }.sortedWith(
              Comparator { a, b ->
                compareVersions(parseVersion(b.version) ?: emptyList(), parseVersion(a.version) ?: emptyList())
              },
            )
      }

    if (releases != null) {
      lastSeenVersionStore.setLastSeenVersion(currentVersion)
      Log.i(TAG, "Showing changelog: ${filtered.size} entries from $lastSeen to $currentVersion")
    }
    return filtered
  }
}
