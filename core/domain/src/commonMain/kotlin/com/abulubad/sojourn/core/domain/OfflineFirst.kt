package com.abulubad.sojourn.core.domain

import com.abulubad.sojourn.core.common.AppError
import com.abulubad.sojourn.core.common.AppResult
import com.abulubad.sojourn.core.common.Loadable
import com.abulubad.sojourn.core.common.errorOrNull
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Combines a database [read] with one [refresh] into the [Loadable] a screen renders (ADR 0007).
 *
 * The cache is shown from the first emission and never dropped because the refresh failed. A value that
 * [isCached] rejects (an empty list before the first refresh) counts as "nothing cached" until a successful refresh
 * confirms it really is empty; only then does it reach the screen, as the empty state.
 */
internal fun <T> offlineFirst(
    read: Flow<T>,
    isCached: (T) -> Boolean,
    refresh: suspend () -> AppResult<Unit>,
): Flow<Loadable<T>> = channelFlow {
    val fetch = MutableStateFlow(Fetch.Running)
    launch {
        val result = refresh()
        // Read once more after a success: the database now holds the fresh data, so "empty" is the real answer.
        val confirmedEmpty = result.isSuccess && !isCached(read.first())
        fetch.value = Fetch(running = false, error = result.errorOrNull(), confirmedEmpty = confirmedEmpty)
    }
    combine(read, fetch) { data, status ->
        Loadable(
            data = data.takeIf { isCached(it) || status.confirmedEmpty },
            refreshing = status.running,
            error = status.error,
        )
    }.collect { send(it) }
}

private data class Fetch(val running: Boolean, val error: AppError? = null, val confirmedEmpty: Boolean = false) {
    companion object {
        val Running = Fetch(running = true)
    }
}
