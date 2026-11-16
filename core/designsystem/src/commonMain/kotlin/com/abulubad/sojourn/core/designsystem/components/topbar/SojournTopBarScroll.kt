package com.abulubad.sojourn.core.designsystem.components.topbar

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity

/**
 * How solid a [SojournTopBar] over a photo should be for a scrolling column: 0 at the top, 1 after
 * [SojournTopBarDefaults.SolidAfter]. The returned lambda reads the scroll position only when the bar draws.
 */
@Composable
fun rememberTopBarSolidFraction(scrollState: ScrollState): () -> Float {
    val distance = with(LocalDensity.current) { SojournTopBarDefaults.SolidAfter.toPx() }
    return remember(scrollState, distance) { { (scrollState.value / distance).coerceIn(0f, 1f) } }
}

/** The same for a lazy list: solid as soon as the first item has scrolled [SojournTopBarDefaults.SolidAfter]. */
@Composable
fun rememberTopBarSolidFraction(listState: LazyListState): () -> Float {
    val distance = with(LocalDensity.current) { SojournTopBarDefaults.SolidAfter.toPx() }
    return remember(listState, distance) {
        {
            if (listState.firstVisibleItemIndex > 0) {
                1f
            } else {
                (listState.firstVisibleItemScrollOffset / distance).coerceIn(0f, 1f)
            }
        }
    }
}
