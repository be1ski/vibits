package space.be1ski.vibits.feature.auth.data.internal

import space.be1ski.vibits.feature.auth.data.LocalCredentials

private const val ACCOUNT_BASE_URL = "base_url"
private const val ACCOUNT_TOKEN = "token"

internal object MacOsKeychainCredentials {
  fun load(service: String): LocalCredentials? {
    val baseUrl = readKeychainPassword(service, ACCOUNT_BASE_URL)?.takeIf { it.isNotBlank() }
    val token = readKeychainPassword(service, ACCOUNT_TOKEN)?.takeIf { it.isNotBlank() }
    return if (baseUrl != null && token != null) {
      LocalCredentials(baseUrl = baseUrl, token = token)
    } else {
      null
    }
  }

  fun save(
    service: String,
    credentials: LocalCredentials,
  ): Boolean {
    val savedBaseUrl = writeKeychainPassword(service, ACCOUNT_BASE_URL, credentials.baseUrl)
    val savedToken = writeKeychainPassword(service, ACCOUNT_TOKEN, credentials.token)
    return savedBaseUrl && savedToken
  }

  private fun readKeychainPassword(
    service: String,
    account: String,
  ): String? =
    try {
      val process =
        Runtime.getRuntime().exec(
          arrayOf(
            "/usr/bin/security",
            "find-generic-password",
            "-s",
            service,
            "-a",
            account,
            "-w",
          ),
        )
      val result =
        process.inputStream
          .bufferedReader()
          .readText()
          .trim()
      val exitCode = process.waitFor()
      if (exitCode == 0) result else null
    } catch (_: Exception) {
      null
    }

  private fun writeKeychainPassword(
    service: String,
    account: String,
    password: String,
  ): Boolean =
    try {
      val process =
        Runtime.getRuntime().exec(
          arrayOf(
            "/usr/bin/security",
            "add-generic-password",
            "-s",
            service,
            "-a",
            account,
            "-w",
            password,
            "-U",
          ),
        )
      process.waitFor() == 0
    } catch (_: Exception) {
      false
    }
}
