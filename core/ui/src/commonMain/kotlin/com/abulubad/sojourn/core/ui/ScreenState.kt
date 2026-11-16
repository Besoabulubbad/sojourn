package com.abulubad.sojourn.core.ui

import androidx.compose.runtime.Immutable
import com.abulubad.sojourn.core.common.AppError

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
    }
}

/** The data on screen right now, from Content or cached Offline. */
val <T> ScreenState<T>.dataOrNull: T?
    get() = when (this) {
        is ScreenState.Content -> data
        is ScreenState.Offline -> cached
        ScreenState.Loading, ScreenState.Empty, is ScreenState.Error -> null
    }
