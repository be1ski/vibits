package space.be1ski.vibits.shared.feature.memos.data.offline

import kotlinx.serialization.json.Json
import java.io.File
import java.nio.file.Paths

/**
 * Desktop implementation storing memos in Documents/Vibits folder.
 */
actual class OfflineMemoStorage {
  private val fileName = "memos.json"
  private val json =
    Json {
      ignoreUnknownKeys = true
      prettyPrint = true
    }

  actual fun load(): OfflineMemosFileDto {
    val file = getFile()
    if (!file.exists()) {
      return OfflineMemosFileDto()
    }
    return runCatching {
      val content = file.readText()
      json.decodeFromString<OfflineMemosFileDto>(content)
    }.getOrDefault(OfflineMemosFileDto())
  }

  actual fun save(data: OfflineMemosFileDto) {
    val file = getFile()
    runCatching {
      file.parentFile?.mkdirs()
      file.writeText(json.encodeToString(OfflineMemosFileDto.serializer(), data))
    }
  }

  private fun getFile(): File {
    val home = System.getProperty("user.home")
    val documentsDir = Paths.get(home, "Documents", "Vibits").toFile()
    return File(documentsDir, fileName)
  }
}
