package space.be1ski.vibits.feature.changelog.data

import dev.zacsweers.metro.Inject
import space.be1ski.vibits.feature.changelog.domain.model.ChangelogEntry
import space.be1ski.vibits.feature.changelog.domain.repository.ChangelogRepository

@Inject
class ChangelogRepositoryImpl(
  private val api: GitHubReleasesApi,
) : ChangelogRepository {
  override suspend fun getReleases(): List<ChangelogEntry> =
    api.getReleases().map { dto ->
      ChangelogEntry(
        version = dto.tagName.removePrefix("v"),
        title = dto.name.ifBlank { dto.tagName },
        body = dto.body,
        date = dto.publishedAt.substringBefore("T"),
      )
    }
}
