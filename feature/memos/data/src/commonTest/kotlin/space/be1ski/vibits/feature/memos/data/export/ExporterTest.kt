package space.be1ski.vibits.feature.memos.data.export

import space.be1ski.vibits.core.platform.export.FileExporter
import space.be1ski.vibits.feature.memos.data.offline.OfflineMemosFileDto
import space.be1ski.vibits.feature.memos.data.platform.OfflineMemoStorage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Instant

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

  @Test
  fun `when exportLogs then generates file name with logs prefix and txt extension`() {
    val fakeFileExporter = FakeFileExporter(exportResult = "/path/to/file")
    val fakeStorage = FakeOfflineMemoStorage()
    val fakeClock = FakeClock(Instant.parse("2024-01-15T10:30:45.123Z"))
    val exporter = Exporter(fakeFileExporter, fakeStorage, fakeClock)

    exporter.exportLogs()

    assertEquals("logs_2024-01-15T10-30-45.txt", fakeFileExporter.lastFileName)
  }

  @Test
  fun `when exportMemos then generates file name with memos prefix and json extension`() {
    val fakeFileExporter = FakeFileExporter(exportResult = "/path/to/file")
    val fakeStorage = FakeOfflineMemoStorage()
    val fakeClock = FakeClock(Instant.parse("2024-01-15T10:30:45.123Z"))
    val exporter = Exporter(fakeFileExporter, fakeStorage, fakeClock)

    exporter.exportMemos()

    assertEquals("memos_2024-01-15T10-30-45.json", fakeFileExporter.lastFileName)
  }
}

private class FakeFileExporter(
  private val exportResult: String?,
) : FileExporter {
  var lastFileName: String? = null
    private set

  override fun export(
    fileName: String,
    content: String,
  ): String? {
    lastFileName = fileName
    return exportResult
  }
}

private class FakeOfflineMemoStorage : OfflineMemoStorage {
  override fun load(): OfflineMemosFileDto = OfflineMemosFileDto()

  override fun save(data: OfflineMemosFileDto) = Unit
}

private class FakeClock(
  private val fixedInstant: Instant,
) : Clock {
  override fun now(): Instant = fixedInstant
}
