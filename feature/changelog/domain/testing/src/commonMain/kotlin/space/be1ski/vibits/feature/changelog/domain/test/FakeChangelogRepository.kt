package space.be1ski.vibits.feature.changelog.domain.test

import space.be1ski.vibits.feature.changelog.domain.model.ChangelogEntry
import space.be1ski.vibits.feature.changelog.domain.repository.ChangelogRepository

class FakeChangelogRepository : ChangelogRepository {
  var releasesResult: Result<List<ChangelogEntry>> = Result.success(emptyList())
  var getReleaseCalls: Int = 0
    private set

  override suspend fun getReleases(): List<ChangelogEntry> {
    getReleaseCalls++
    return releasesResult.getOrThrow()
  }
}
