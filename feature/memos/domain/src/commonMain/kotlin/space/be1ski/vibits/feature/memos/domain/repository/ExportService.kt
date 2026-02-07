package space.be1ski.vibits.feature.memos.domain.repository

import space.be1ski.vibits.feature.memos.domain.model.ExportResult

interface ExportService {
  fun exportLogs(): ExportResult

  fun exportMemos(): ExportResult
}
