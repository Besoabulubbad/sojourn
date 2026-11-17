package com.abulubad.sojourn.core.network.ktor

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout

/**
 * The app's Ktor client: the platform engine on the classpath (OkHttp on Android, Darwin on iOS), non-2xx
 * responses as `ResponseException`, and a request timeout so a dead network becomes `AppError.Offline`.
 */
fun sojournHttpClient(): HttpClient = HttpClient {
    expectSuccess = true
    install(HttpTimeout) { requestTimeoutMillis = REQUEST_TIMEOUT_MILLIS }
}

private const val REQUEST_TIMEOUT_MILLIS = 15_000L
