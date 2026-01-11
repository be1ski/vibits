package space.be1ski.memos.shared.config

import java.io.File
import java.util.Properties

actual fun loadLocalCredentials(): LocalCredentials {
  val props = Properties()
  val file = File(System.getProperty("user.dir"), "local.properties")
  if (file.exists()) {
    file.inputStream().use { props.load(it) }
  }

  val baseUrl = props.getProperty("memos.baseUrl", "https://memos.int.be1ski.space").trim()
  val token = props.getProperty("memos.token", "REDACTED_TOKEN").trim()
  return LocalCredentials(baseUrl = baseUrl, token = token)
}
