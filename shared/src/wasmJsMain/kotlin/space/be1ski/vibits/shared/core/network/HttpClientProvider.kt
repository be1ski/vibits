package space.be1ski.vibits.shared.core.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Creates a JS [HttpClient] configured for the Memos API.
 */
actual fun createHttpClient(): HttpClient {
  return HttpClient(Js) {
    install(ContentNegotiation) {
      json(Json {
        ignoreUnknownKeys = true
        isLenient = true
      })
    }
    install(Logging) {
      level = LogLevel.INFO
    }
  }
}
