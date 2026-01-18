package space.be1ski.vibits.shared.feature.memos.data.platform

import android.os.Environment
import kotlinx.serialization.json.Json
import space.be1ski.vibits.shared.app.data.AndroidContextHolder
import space.be1ski.vibits.shared.feature.memos.data.offline.OfflineMemosFileDto
import java.io.File

actual fun createOfflineMemoStorage(): OfflineMemoStorage = AndroidOfflineMemoStorage()

private class AndroidOfflineMemoStorage : OfflineMemoStorage {
  private val fileName = "memos.json"
  private val json =
    Json {
      ignoreUnknownKeys = true
      prettyPrint = true
    }

  override fun load(): OfflineMemosFileDto {
    val file = getFile()?.takeIf { it.exists() } ?: return OfflineMemosFileDto()
    return runCatching {
      val content = file.readText()
      json.decodeFromString<OfflineMemosFileDto>(content)
    }.getOrDefault(OfflineMemosFileDto())
  }

  override fun save(data: OfflineMemosFileDto) {
    val file = getFile() ?: return
    runCatching {
      file.parentFile?.mkdirs()
      file.writeText(json.encodeToString(OfflineMemosFileDto.serializer(), data))
    }
  }

  private fun getFile(): File? {
    if (!AndroidContextHolder.isReady()) {
      return null
    }
    val documentsDir = AndroidContextHolder.context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
    return documentsDir?.let { File(it, fileName) }
  }
}
