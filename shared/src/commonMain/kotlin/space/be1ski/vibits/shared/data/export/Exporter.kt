package space.be1ski.vibits.shared.data.export

import dev.zacsweers.metro.Inject
import kotlinx.serialization.json.Json
import space.be1ski.vibits.shared.core.export.FileExporter
import space.be1ski.vibits.shared.core.logging.Log
import space.be1ski.vibits.shared.feature.memos.data.offline.OfflineMemoStorage
import space.be1ski.vibits.shared.feature.memos.data.offline.OfflineMemosFileDto
import kotlin.time.Clock

/**
 * Handles exporting app data to files.
 */
@Inject
class Exporter(
  private val fileExporter: FileExporter = FileExporter(),
  private val offlineMemoStorage: OfflineMemoStorage = OfflineMemoStorage(),
) {
  private val json =
    Json {
      ignoreUnknownKeys = true
      prettyPrint = true
    }

  /**
   * Export logs to a text file.
   * @return ExportResult with file name on success or error message on failure
   */
  fun exportLogs(): ExportResult {
    val fileName = generateFileName("logs", "txt")
    val content = Log.export()
    val path = fileExporter.export(fileName, content)
    return if (path != null) {
      ExportResult.Success(fileName)
    } else {
      ExportResult.Failure
    }
  }

  /**
   * Export offline memos to a JSON file.
   * @return ExportResult with file name on success or error message on failure
   */
  fun exportMemos(): ExportResult {
    val fileName = generateFileName("memos", "json")
    val data = offlineMemoStorage.load()
    val content = json.encodeToString(OfflineMemosFileDto.serializer(), data)
    val path = fileExporter.export(fileName, content)
    return if (path != null) {
      ExportResult.Success(fileName)
    } else {
      ExportResult.Failure
    }
  }

  private fun generateFileName(
    prefix: String,
    extension: String,
  ): String {
    val timestamp =
      Clock.System
        .now()
        .toString()
        .replace(":", "-")
        .replace(".", "-")
        .take(TIMESTAMP_LENGTH)
    return "${prefix}_$timestamp.$extension"
  }

  private companion object {
    const val TIMESTAMP_LENGTH = 19
  }
}

sealed interface ExportResult {
  data class Success(
    val fileName: String,
  ) : ExportResult

  data object Failure : ExportResult
}
