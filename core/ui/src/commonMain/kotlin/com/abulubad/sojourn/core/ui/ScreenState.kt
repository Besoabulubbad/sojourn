package com.abulubad.sojourn.core.ui

import androidx.compose.runtime.Immutable
import com.abulubad.sojourn.core.common.AppError
import com.abulubad.sojourn.core.common.Loadable

/**
 * The five states every data screen implements (I-05). A sealed type, so `when` over it must be exhaustive and a
 * screen cannot forget offline or empty.
 *
 * [Offline] may carry cached content: the screen shows it under an offline banner rather than a blank page.
 */
@Immutable
sealed interface ScreenState<out T> {
    data object Loading : ScreenState<Nothing>
    data class Content<T>(val data: T) : ScreenState<T>
    data object Empty : ScreenState<Nothing>
    data class Error(val error: AppError) : ScreenState<Nothing>
    data class Offline<T>(val cached: T?) : ScreenState<T>

    companion object {
        /** Maps a failure to Offline (keeping [cached]) or Error, the rule every ViewModel follows. */
        fun <T> fromError(error: AppError, cached: T? = null): ScreenState<T> =
            if (error is AppError.Offline) Offline(cached) else Error(error)

        /** Content, or Empty when [isEmpty] says the data has nothing to show. */
        fun <T> fromData(data: T, isEmpty: (T) -> Boolean): ScreenState<T> = if (isEmpty(data)) Empty else Content(data)

        /**
         * The state for an offline-first read (ADR 0007), the rule every data screen follows:
         *
         * - Nothing cached: [Loading] while fetching, then [Offline] or [Error] if the fetch failed.
         * - Cached data: [Offline] with it when the last fetch had no connection, otherwise [Content]. A server error
         *   never replaces content already on screen; the ViewModel may mention it in a snackbar.
         * - Cached but [isEmpty]: [Empty], or [Offline] / [Error] when the fetch that should fill it failed.
         */
        fun <T> from(loadable: Loadable<T>, isEmpty: (T) -> Boolean): ScreenState<T> {
            val data = loadable.data
            val error = loadable.error
            return when {
                data == null -> if (error != null) fromError(error) else Loading
                isEmpty(data) -> if (error != null) fromError(error) else Empty
                error is AppError.Offline -> Offline(data)
                else -> Content(data)
            }
        }
    }
}

/** The data on screen right now, from Content or cached Offline. */
val <T> ScreenState<T>.dataOrNull: T?
    get() = when (this) {
        is ScreenState.Content -> data
        is ScreenState.Offline -> cached
        ScreenState.Loading, ScreenState.Empty, is ScreenState.Error -> null
    }
