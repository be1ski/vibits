package space.be1ski.vibits.shared.core.platform.export

import java.io.File
import java.nio.file.Paths

actual fun createFileExporter(): FileExporter = DesktopFileExporter()

/**
 * Desktop implementation that saves files to ~/Documents/Vibits folder.
 */
private class DesktopFileExporter : FileExporter {
  override fun export(
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
