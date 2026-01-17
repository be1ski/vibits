package space.be1ski.vibits.shared.core.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Android-specific HTTP client configuration.
 */
actual fun createHttpClient(): HttpClient =
  HttpClient(OkHttp) {
    install(ContentNegotiation) {
      json(
        Json {
          ignoreUnknownKeys = true
          isLenient = true
        },
      )
    }
    install(Logging) {
      logger =
        object : Logger {
          override fun log(message: String) {
            println("HTTP | $message")
          }
        }
      level = LogLevel.INFO
      sanitizeHeader { it == HttpHeaders.Authorization }
    }
  }
