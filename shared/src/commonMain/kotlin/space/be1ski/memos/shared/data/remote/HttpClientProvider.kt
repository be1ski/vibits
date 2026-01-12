package space.be1ski.memos.shared.data.remote

import io.ktor.client.HttpClient

/**
 * Creates a platform-specific [HttpClient] configured for the Memos API.
 */
expect fun createHttpClient(): HttpClient
