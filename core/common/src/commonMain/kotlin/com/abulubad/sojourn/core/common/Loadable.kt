package com.abulubad.sojourn.core.common

/**
 * What an offline-first read knows right now (ADR 0007): the [data] on the device, whether a [refreshing] fetch is
 * running, and the [error] the last fetch ended with. Use cases emit it; `core:ui` maps it to a `ScreenState`, so
 * every screen applies the same rule for loading, empty, error and offline (I-05).
 *
 * [data] is null when nothing usable is cached yet. Cached data with an [error] means "show it, and say why it may
 * be stale"; it is never thrown away because a refresh failed.
 */
data class Loadable<out T>(
    val data: T?,
    val refreshing: Boolean = false,
    val error: AppError? = null,
) {
    companion object {
        /** Nothing cached, a fetch running: the first launch. */
        val Loading: Loadable<Nothing> = Loadable(data = null, refreshing = true)
    }
}

/** Transforms the data, keeping the refresh status. */
inline fun <T, R> Loadable<T>.map(transform: (T) -> R): Loadable<R> =
    Loadable(data?.let(transform), refreshing, error)
