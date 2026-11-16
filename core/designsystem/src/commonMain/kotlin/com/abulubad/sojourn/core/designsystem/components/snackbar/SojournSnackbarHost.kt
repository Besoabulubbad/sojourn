package com.abulubad.sojourn.core.designsystem.components.snackbar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull

/** How a snackbar ended. */
enum class SojournSnackbarResult { ActionPerformed, Dismissed }

/** The snackbar on screen and the way to answer it. */
class SojournSnackbarRequest internal constructor(val message: String, val actionLabel: String?) {
    internal val outcome = CompletableDeferred<SojournSnackbarResult>()

    /** Called when the guest taps the action. */
    fun performAction() {
        outcome.complete(SojournSnackbarResult.ActionPerformed)
    }
}

/**
 * Queues snackbars one at a time (Snackbar card): each stays 5 seconds unless its action is taken. A screen keeps
 * one in its `XxxRoute` and calls [show] from an effect.
 */
@Stable
class SojournSnackbarHostState {
    private val queue = Mutex()

    /** The snackbar on screen, or null. */
    var current: SojournSnackbarRequest? by mutableStateOf(null)
        private set

    /** Shows [message] with an optional [actionLabel] and suspends until it is acted on or times out. */
    suspend fun show(message: String, actionLabel: String? = null): SojournSnackbarResult = queue.withLock {
        val request = SojournSnackbarRequest(message, actionLabel)
        current = request
        try {
            withTimeoutOrNull(VISIBLE_MILLIS) { request.outcome.await() } ?: SojournSnackbarResult.Dismissed
        } finally {
            current = null
        }
    }

    private companion object {
        /** "Snackbars last 5 seconds" (Snackbar card). */
        const val VISIBLE_MILLIS = 5_000L
    }
}

/** Renders the current snackbar of [state] as a [SojournSnackbar]. `SojournScaffold` places it above the bottom bar. */
@Composable
fun SojournSnackbarHost(state: SojournSnackbarHostState, modifier: Modifier = Modifier) {
    val request = state.current ?: return
    SojournSnackbar(
        message = request.message,
        modifier = modifier,
        actionLabel = request.actionLabel,
        onAction = request::performAction,
    )
}
