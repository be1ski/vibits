package space.be1ski.vibits.shared.core.export

import java.io.File
import java.nio.file.Paths

/**
 * Desktop implementation that saves files to ~/Documents/Vibits folder.
 */
actual class FileExporter {
  actual fun export(
    fileName: String,
    content: String,
  ): String? =
    runCatching {
      val file = getExportFile(fileName)
      file.parentFile?.mkdirs()
      file.writeText(content)
      file.absolutePath
    }.getOrNull()

  private fun getExportFile(fileName: String): File {
    val home = System.getProperty("user.home")
    val exportDir = Paths.get(home, "Documents", "Vibits").toFile()
    return File(exportDir, fileName)
  }
}
