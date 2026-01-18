package space.be1ski.vibits.shared.core.export

/**
 * Platform-specific file exporter that saves content to Downloads/Documents folder.
 * Returns the file path on success or null on failure.
 */
expect class FileExporter() {
  /**
   * Export text content to a file.
   * @param fileName The name of the file (e.g., "logs.txt", "memos.json")
   * @param content The content to write
   * @return The file path on success, null on failure
   */
  fun export(
    fileName: String,
    content: String,
  ): String?
}
