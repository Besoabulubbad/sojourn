package com.abulubad.sojourn.core.designsystem.components.internal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import com.abulubad.sojourn.core.designsystem.components.staycard.SojournStayCardDefaults
import com.abulubad.sojourn.core.designsystem.tokens.SojournShapes
import com.abulubad.sojourn.core.model.Photo

/**
 * Swipeable photos. The pager follows the layout direction, so in Arabic the next photo comes from the left, and
 * it composes [preload] pages beyond the visible one so the next photos are already loading.
 */
@Composable
internal fun PhotoPager(
    photos: List<Photo>,
    modifier: Modifier = Modifier,
    preload: Int = 1,
    state: PagerState = rememberPagerState { photos.size },
    page: @Composable BoxScope.(index: Int, photo: Photo) -> Unit = { _, photo ->
        PhotoSlot(photo, contentDescription = null, modifier = Modifier.fillMaxSize())
    },
    overlay: @Composable BoxScope.(PagerState) -> Unit = {},
) {
    Box(modifier) {
        HorizontalPager(state = state, beyondViewportPageCount = preload, modifier = Modifier.fillMaxSize()) { index ->
            Box(Modifier.fillMaxSize()) { page(index, photos[index]) }
        }
        overlay(state)
    }
}

/** The card's page dots: white over the photo, the current one opaque. Decorative; the counter is spoken. */
@Composable
internal fun PageDots(count: Int, current: Int, modifier: Modifier = Modifier) {
    if (count < 2) return
    Row(
        modifier.clearAndSetSemantics {},
        horizontalArrangement = Arrangement.spacedBy(SojournStayCardDefaults.DotGap),
    ) {
        repeat(count) { index ->
            val alpha = if (index == current) 1f else SojournStayCardDefaults.DOT_INACTIVE_ALPHA
            // Dots sit on photos, whatever the theme: white is the only colour that reads on any photo.
            Box(
                Modifier
                    .size(SojournStayCardDefaults.DotSize)
                    .background(Color.White.copy(alpha = alpha), SojournShapes.full),
            )
        }
    }
}
