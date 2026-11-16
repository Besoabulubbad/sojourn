package com.abulubad.sojourn.core.common

import kotlinx.coroutines.CancellationException

/**
 * Why an operation failed, in terms a screen can map to a `ScreenState` (I-05): [Offline] becomes the offline
 * state, everything else the error state. Messages are never carried here; the UI picks a resource by type.
 */
sealed interface AppError {
    /** No connectivity, or the request timed out. */
    data object Offline : AppError

    /** The server answered with an error. */
    data class Remote(val code: Int, val detail: String? = null) : AppError

    /** The thing asked for does not exist (any more). */
    data class NotFound(val id: String) : AppError

    /** The input broke a rule before leaving the device. */
    data class Validation(val reason: String) : AppError

    /** Anything unforeseen; the cause is logged, never shown. */
    data class Unexpected(val cause: Throwable? = null) : AppError
}

/** Success or an [AppError]. Preferred over exceptions across the domain boundary. */
sealed interface AppResult<out T> {
    data class Success<T>(val value: T) : AppResult<T>
    data class Failure(val error: AppError) : AppResult<Nothing>

    val isSuccess: Boolean get() = this is Success
}

inline fun <T, R> AppResult<T>.map(transform: (T) -> R): AppResult<R> = when (this) {
    is AppResult.Success -> AppResult.Success(transform(value))
    is AppResult.Failure -> this
}

inline fun <T, R> AppResult<T>.fold(onSuccess: (T) -> R, onFailure: (AppError) -> R): R = when (this) {
    is AppResult.Success -> onSuccess(value)
    is AppResult.Failure -> onFailure(error)
}

fun <T> AppResult<T>.getOrNull(): T? = (this as? AppResult.Success)?.value

fun <T> AppResult<T>.errorOrNull(): AppError? = (this as? AppResult.Failure)?.error

/**
 * Runs [block] and wraps any exception as [AppError.Unexpected], rethrowing [CancellationException] so structured
 * concurrency keeps working. Callers that know better map specific exceptions before reaching this.
 */
inline fun <T> runCatchingApp(block: () -> T): AppResult<T> = try {
    AppResult.Success(block())
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    AppResult.Failure(AppError.Unexpected(e))
}
