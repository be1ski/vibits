package space.be1ski.vibits.shared.core.export

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create

/**
 * iOS implementation that saves files to Documents directory.
 */
actual class FileExporter {
  @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
  actual fun export(
    fileName: String,
    content: String,
  ): String? =
    runCatching {
      val path = getFilePath(fileName) ?: return null
      val bytes = content.encodeToByteArray()
      val nsData =
        bytes.usePinned { pinned ->
          NSData.create(bytes = pinned.addressOf(0), length = bytes.size.toULong())
        }
      NSFileManager.defaultManager.createFileAtPath(path, nsData, null)
      path
    }.getOrNull()

  @OptIn(ExperimentalForeignApi::class)
  private fun getFilePath(fileName: String): String? {
    val paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, true)
    val documentsDir = paths.firstOrNull() as? String ?: return null
    return "$documentsDir/$fileName"
  }
}
