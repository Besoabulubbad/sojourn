package com.abulubad.sojourn.core.data

import com.abulubad.sojourn.core.common.AppClock
import com.abulubad.sojourn.core.common.AppResult
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

/**
 * When a refresh goes to the network (ADR 0007). Screens ask for a refresh every time they appear; this keeps that
 * cheap: a key refreshed successfully within [maxAge] is skipped unless forced (pull to refresh, retry). Refreshes
 * run one at a time, so two screens appearing together make one call and the second finds the data fresh.
 *
 * Freshness lives in memory: each launch refreshes once, and the database shows the last content meanwhile.
 */
internal class RefreshPolicy(private val clock: AppClock, private val maxAge: Duration = DEFAULT_MAX_AGE) {

    private val lock = Mutex()
    private val refreshedAt = mutableMapOf<String, Instant>()

    /** Runs [refresh] for [key] unless it is fresh; a skipped refresh is a success. */
    suspend fun run(key: String, force: Boolean, refresh: suspend () -> AppResult<Unit>): AppResult<Unit> =
        lock.withLock {
            val last = refreshedAt[key]
            if (!force && last != null && clock.now() - last < maxAge) return@withLock AppResult.Success(Unit)
            refresh().also { if (it.isSuccess) refreshedAt[key] = clock.now() }
        }

    companion object {
        val DEFAULT_MAX_AGE: Duration = 15.minutes
    }
}
