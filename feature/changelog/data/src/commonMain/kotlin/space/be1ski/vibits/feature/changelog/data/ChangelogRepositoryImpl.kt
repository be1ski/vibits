package space.be1ski.vibits.feature.changelog.data

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import space.be1ski.vibits.core.platform.di.AppScope
import space.be1ski.vibits.feature.changelog.domain.model.ChangelogEntry
import space.be1ski.vibits.feature.changelog.domain.repository.ChangelogRepository

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
internal class ChangelogRepositoryImpl(
  private val api: GitHubReleasesApi,
) : ChangelogRepository {
  override suspend fun getReleases(): List<ChangelogEntry> =
    api.getReleases().map { dto ->
      ChangelogEntry(
        version = dto.tagName.removePrefix("v"),
        title = dto.name.ifBlank { dto.tagName },
        body = dto.body,
        date = dto.publishedAt.substringBefore("T"),
        hasDmgAsset = dto.assets.any { asset -> asset.name.endsWith(".dmg") },
      )
    }
}
