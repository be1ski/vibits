package space.be1ski.vibits.feature.changelog.domain.repository

interface LastSeenVersionStore {
  fun getLastSeenVersion(): String?

  fun setLastSeenVersion(version: String)
}
