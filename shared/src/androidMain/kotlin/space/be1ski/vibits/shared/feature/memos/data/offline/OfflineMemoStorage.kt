package space.be1ski.vibits.shared.feature.memos.data.offline

import android.os.Environment
import kotlinx.serialization.json.Json
import space.be1ski.vibits.shared.data.local.AndroidContextHolder
import java.io.File

/**
 * Android implementation storing memos in Documents folder.
 */
actual class OfflineMemoStorage {
  private val fileName = "memos.json"
  private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }

  actual fun load(): OfflineMemosFileDto {
    val file = getFile()?.takeIf { it.exists() } ?: return OfflineMemosFileDto()
    return runCatching {
      val content = file.readText()
      json.decodeFromString<OfflineMemosFileDto>(content)
    }.getOrDefault(OfflineMemosFileDto())
  }

  actual fun save(data: OfflineMemosFileDto) {
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
