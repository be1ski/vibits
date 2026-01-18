package space.be1ski.vibits.shared.app.data

import space.be1ski.vibits.shared.core.platform.export.FileExporter
import space.be1ski.vibits.shared.feature.memos.data.offline.OfflineMemosFileDto
import space.be1ski.vibits.shared.feature.memos.data.platform.OfflineMemoStorage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ExporterTest {
  @Test
  fun `when exportLogs succeeds then returns Success with file path`() {
    val fakeFileExporter = FakeFileExporter(exportResult = "/path/to/logs.txt")
    val fakeStorage = FakeOfflineMemoStorage()
    val exporter = Exporter(fakeFileExporter, fakeStorage)

    val result = exporter.exportLogs()

    assertTrue(result is ExportResult.Success)
    assertEquals("/path/to/logs.txt", result.filePath)
  }

  @Test
  fun `when exportLogs fails then returns Failure`() {
    val fakeFileExporter = FakeFileExporter(exportResult = null)
    val fakeStorage = FakeOfflineMemoStorage()
    val exporter = Exporter(fakeFileExporter, fakeStorage)

    val result = exporter.exportLogs()

    assertTrue(result is ExportResult.Failure)
  }

  @Test
  fun `when exportMemos succeeds then returns Success with file path`() {
    val fakeFileExporter = FakeFileExporter(exportResult = "/path/to/memos.json")
    val fakeStorage = FakeOfflineMemoStorage()
    val exporter = Exporter(fakeFileExporter, fakeStorage)

    val result = exporter.exportMemos()

    assertTrue(result is ExportResult.Success)
    assertEquals("/path/to/memos.json", result.filePath)
  }

  @Test
  fun `when exportMemos fails then returns Failure`() {
    val fakeFileExporter = FakeFileExporter(exportResult = null)
    val fakeStorage = FakeOfflineMemoStorage()
    val exporter = Exporter(fakeFileExporter, fakeStorage)

    val result = exporter.exportMemos()

    assertTrue(result is ExportResult.Failure)
  }
}

private class FakeFileExporter(
  private val exportResult: String?,
) : FileExporter {
  override fun export(
    fileName: String,
    content: String,
  ): String? = exportResult
}

private class FakeOfflineMemoStorage : OfflineMemoStorage {
  override fun load(): OfflineMemosFileDto = OfflineMemosFileDto()

  override fun save(data: OfflineMemosFileDto) = Unit
}
