package space.be1ski.memos.shared.feature.memos.data.offline

import kotlinx.browser.localStorage
import kotlinx.serialization.json.Json

/**
 * Web implementation storing memos in localStorage.
 */
actual class OfflineMemoStorage {
  private val storageKey = "memos_offline_data"
  private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }

  actual fun load(): OfflineMemosFileDto {
    return runCatching {
      val content = localStorage.getItem(storageKey) ?: return OfflineMemosFileDto()
      json.decodeFromString<OfflineMemosFileDto>(content)
    }.getOrDefault(OfflineMemosFileDto())
  }

  actual fun save(data: OfflineMemosFileDto) {
    runCatching {
      val content = json.encodeToString(OfflineMemosFileDto.serializer(), data)
      localStorage.setItem(storageKey, content)
    }
  }
}
