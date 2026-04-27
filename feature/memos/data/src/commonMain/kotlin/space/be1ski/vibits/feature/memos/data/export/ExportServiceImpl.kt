package space.be1ski.vibits.feature.memos.data.export

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.serialization.json.Json
import space.be1ski.vibits.core.platform.di.AppScope
import space.be1ski.vibits.core.platform.export.FileExporter
import space.be1ski.vibits.core.utils.logging.Log
import space.be1ski.vibits.feature.memos.data.offline.OfflineMemosFileDto
import space.be1ski.vibits.feature.memos.data.platform.OfflineMemoStorage
import space.be1ski.vibits.feature.memos.domain.model.ExportResult
import space.be1ski.vibits.feature.memos.domain.repository.ExportService
import kotlin.time.Clock

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
internal class ExportServiceImpl(
  private val fileExporter: FileExporter,
  private val offlineMemoStorage: OfflineMemoStorage,
  private val clock: Clock = Clock.System,
) : ExportService {
  private val json =
    Json {
      ignoreUnknownKeys = true
      prettyPrint = true
    }

  override fun exportLogs(): ExportResult {
    val fileName = generateFileName("logs", "txt")
    val content = Log.export()
    val filePath = fileExporter.export(fileName, content)
    return if (filePath != null) {
      ExportResult.Success(filePath)
    } else {
      ExportResult.Failure
    }
  }

  override fun exportMemos(): ExportResult {
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
