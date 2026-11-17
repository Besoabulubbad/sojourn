package com.abulubad.sojourn.core.network.local

import com.abulubad.sojourn.core.common.AppError
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * How [LocalSojournApi] pretends the network behaves (ADR 0007), so loading, error and offline states are
 * reproducible in tests and demos. Change it at runtime from the debug menu or set it up in a test.
 */
class FakeNetworkConditions(
    var latency: Duration = DEFAULT_LATENCY,
    var mode: Mode = Mode.Online,
) {
    /** What every call does after [latency]. */
    enum class Mode {
        /** Calls succeed. */
        Online,

        /** Every call fails with [AppError.Offline]. */
        Offline,

        /** Every call fails with [AppError.Remote] 503. */
        ServerError,

        /** Every third call fails offline; the others succeed. */
        Flaky,
    }

    private var calls = 0

    /** The failure this call should return, if any. */
    internal fun nextFailure(): AppError? {
        calls++
        return when (mode) {
            Mode.Online -> null
            Mode.Offline -> AppError.Offline
            Mode.ServerError -> AppError.Remote(SERVICE_UNAVAILABLE)
            Mode.Flaky -> if (calls % FLAKY_EVERY == 0) AppError.Offline else null
        }
    }

    companion object {
        val DEFAULT_LATENCY = 400.milliseconds
        private const val SERVICE_UNAVAILABLE = 503
        private const val FLAKY_EVERY = 3
    }
}
