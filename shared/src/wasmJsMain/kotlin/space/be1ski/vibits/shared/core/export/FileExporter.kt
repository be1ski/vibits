@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package space.be1ski.vibits.shared.core.export

import kotlinx.browser.document
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag

@Suppress("UNUSED_PARAMETER")
private fun createBlobParts(content: String): JsArray<JsAny?> = js("([content])")

/**
 * Web implementation that triggers a browser download.
 */
actual class FileExporter {
  actual fun export(
    fileName: String,
    content: String,
  ): String? =
    runCatching {
      val parts = createBlobParts(content)
      val blob = Blob(parts, BlobPropertyBag(type = "text/plain"))
      val url = URL.createObjectURL(blob)

      val anchor = document.createElement("a") as HTMLAnchorElement
      anchor.href = url
      anchor.download = fileName
      anchor.click()

      URL.revokeObjectURL(url)
      fileName
    }.getOrNull()
}
