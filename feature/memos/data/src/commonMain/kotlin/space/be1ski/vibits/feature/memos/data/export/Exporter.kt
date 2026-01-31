package space.be1ski.vibits.feature.memos.data.export

import dev.zacsweers.metro.Inject
import kotlinx.serialization.json.Json
import space.be1ski.vibits.core.logging.Log
import space.be1ski.vibits.core.platform.export.FileExporter
import space.be1ski.vibits.feature.memos.data.offline.OfflineMemosFileDto
import space.be1ski.vibits.feature.memos.data.platform.OfflineMemoStorage
import kotlin.time.Clock

/**
 * Handles exporting app data to files.
 */
@Inject
class Exporter(
  private val fileExporter: FileExporter,
  private val offlineMemoStorage: OfflineMemoStorage,
  private val clock: Clock = Clock.System,
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
    val filePath = fileExporter.export(fileName, content)
    return if (filePath != null) {
      ExportResult.Success(filePath)
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
    val filePath = fileExporter.export(fileName, content)
    return if (filePath != null) {
      ExportResult.Success(filePath)
    } else {
      ExportResult.Failure
    }
  }

  private fun generateFileName(
    prefix: String,
    extension: String,
  ): String {
    val timestamp =
      clock
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
    val filePath: String,
  ) : ExportResult

  data object Failure : ExportResult
}
