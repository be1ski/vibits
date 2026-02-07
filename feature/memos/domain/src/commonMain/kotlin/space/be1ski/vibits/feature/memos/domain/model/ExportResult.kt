package space.be1ski.vibits.feature.memos.domain.model

sealed interface ExportResult {
  data class Success(
    val filePath: String,
  ) : ExportResult

  data object Failure : ExportResult
}
