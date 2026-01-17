package space.be1ski.vibits.shared.core.export

import android.os.Environment
import space.be1ski.vibits.shared.data.local.AndroidContextHolder
import java.io.File

/**
 * Android implementation that saves files to Documents folder.
 */
actual class FileExporter {
  actual fun export(
    fileName: String,
    content: String,
  ): String? =
    runCatching {
      if (!AndroidContextHolder.isReady()) return@runCatching null
      val file = getExportFile(fileName) ?: return@runCatching null
      file.parentFile?.mkdirs()
      file.writeText(content)
      file.absolutePath
    }.getOrNull()

  private fun getExportFile(fileName: String): File? {
    val documentsDir =
      AndroidContextHolder.context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
    return documentsDir?.let { File(it, fileName) }
  }
}
