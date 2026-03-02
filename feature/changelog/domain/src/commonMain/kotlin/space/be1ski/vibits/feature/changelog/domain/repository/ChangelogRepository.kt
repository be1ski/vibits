package space.be1ski.vibits.feature.changelog.domain.repository

import space.be1ski.vibits.feature.changelog.domain.model.ChangelogEntry

fun interface ChangelogRepository {
  suspend fun getReleases(): List<ChangelogEntry>
}
