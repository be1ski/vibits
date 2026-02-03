package space.be1ski.vibits.feature.memos.data.platform

import kotlinx.browser.localStorage
import kotlinx.serialization.json.Json
import space.be1ski.vibits.feature.memos.data.offline.OfflineMemosFileDto

actual fun createOfflineMemoStorage(): OfflineMemoStorage = WasmOfflineMemoStorage()

private class WasmOfflineMemoStorage : OfflineMemoStorage {
  private val storageKey = "memos_offline_data"
  private val json =
    Json {
      ignoreUnknownKeys = true
      prettyPrint = true
    }

  override fun load(): OfflineMemosFileDto {
    return runCatching {
      val content = localStorage.getItem(storageKey) ?: return OfflineMemosFileDto()
      json.decodeFromString<OfflineMemosFileDto>(content)
    }.getOrDefault(OfflineMemosFileDto())
  }

  override fun save(data: OfflineMemosFileDto) {
    runCatching {
      val content = json.encodeToString(OfflineMemosFileDto.serializer(), data)
      localStorage.setItem(storageKey, content)
    }
  }
}
