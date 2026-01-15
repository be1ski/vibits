package space.be1ski.vibits.shared.core.network

import io.ktor.client.HttpClient

/**
 * Creates a platform-specific [HttpClient] configured for the Memos API.
 */
expect fun createHttpClient(): HttpClient
