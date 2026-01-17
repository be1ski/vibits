package space.be1ski.vibits.shared.feature.memos.data.offline

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.serialization.json.Json
import platform.Foundation.NSData
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.Foundation.stringWithContentsOfFile

/**
 * iOS implementation storing memos in Documents directory.
 */
actual class OfflineMemoStorage {
  private val fileName = "memos.json"
  private val json =
    Json {
      ignoreUnknownKeys = true
      prettyPrint = true
    }

  @OptIn(ExperimentalForeignApi::class)
  actual fun load(): OfflineMemosFileDto {
    val fileManager = NSFileManager.defaultManager
    val path =
      getFilePath()?.takeIf { fileManager.fileExistsAtPath(it) }
        ?: return OfflineMemosFileDto()
    return runCatching {
      val content = NSString.stringWithContentsOfFile(path, NSUTF8StringEncoding, null) ?: ""
      json.decodeFromString<OfflineMemosFileDto>(content)
    }.getOrDefault(OfflineMemosFileDto())
  }

  @OptIn(ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)
  actual fun save(data: OfflineMemosFileDto) {
    val path = getFilePath() ?: return
    runCatching {
      val content = json.encodeToString(OfflineMemosFileDto.serializer(), data)
      val bytes = content.encodeToByteArray()
      val nsData =
        bytes.usePinned { pinned ->
          NSData.create(bytes = pinned.addressOf(0), length = bytes.size.toULong())
        }
      NSFileManager.defaultManager.createFileAtPath(path, nsData, null)
    }
  }

  @OptIn(ExperimentalForeignApi::class)
  private fun getFilePath(): String? {
    val paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, true)
    val documentsDir = paths.firstOrNull() as? String ?: return null
    return "$documentsDir/$fileName"
  }
}
