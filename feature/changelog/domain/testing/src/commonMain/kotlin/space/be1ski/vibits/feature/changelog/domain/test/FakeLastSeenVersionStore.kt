package space.be1ski.vibits.feature.changelog.domain.test

import space.be1ski.vibits.feature.changelog.domain.repository.LastSeenVersionStore

class FakeLastSeenVersionStore(
  private var version: String? = null,
) : LastSeenVersionStore {
  var setCalls: Int = 0
    private set
  var lastSetVersion: String? = null
    private set

  override fun getLastSeenVersion(): String? = version

  override fun setLastSeenVersion(version: String) {
    setCalls++
    lastSetVersion = version
    this.version = version
  }
}
