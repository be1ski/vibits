package space.be1ski.vibits.shared.core.export

import android.content.Intent
import androidx.core.content.FileProvider
import space.be1ski.vibits.shared.data.local.AndroidContextHolder
import java.io.File

/**
 * Android implementation that shares files via system share sheet.
 */
actual class FileExporter {
  actual fun export(
    fileName: String,
    content: String,
  ): String? =
    runCatching {
      if (!AndroidContextHolder.isReady()) return@runCatching null
      val context = AndroidContextHolder.context

      val exportsDir = File(context.cacheDir, "exports")
      exportsDir.mkdirs()
      val file = File(exportsDir, fileName)
      file.writeText(content)

      val uri =
        FileProvider.getUriForFile(
          context,
          "${context.packageName}.fileprovider",
          file,
        )

      val mimeType =
        when {
          fileName.endsWith(".json") -> "application/json"
          fileName.endsWith(".txt") -> "text/plain"
          else -> "*/*"
        }

      val shareIntent =
        Intent(Intent.ACTION_SEND).apply {
          type = mimeType
          putExtra(Intent.EXTRA_STREAM, uri)
          addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

      val chooser =
        Intent.createChooser(shareIntent, null).apply {
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
      context.startActivity(chooser)

      fileName
    }.getOrNull()
}
