package space.be1ski.memos.shared.network

import io.ktor.client.HttpClient

expect fun createHttpClient(): HttpClient
