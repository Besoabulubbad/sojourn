package com.abulubad.sojourn.core.designsystem.components.staycard

import androidx.compose.ui.unit.dp

/** Measurements from the StayCard card. */
object SojournStayCardDefaults {
    /** Photo shape in the full layout. */
    const val PHOTO_ASPECT_RATIO = 16f / 10f

    /** At most this many photos swipe inside a card; the rest are on Details. */
    const val MAX_PHOTOS = 5

    /** The compact layout's photo. */
    val CompactPhotoWidth = 96.dp
    val CompactPhotoMinHeight = 104.dp

    /** Between the details lines. */
    val LineGap = 2.dp

    /** The page dots. */
    val DotSize = 6.dp
    val DotGap = 4.dp
    const val DOT_INACTIVE_ALPHA = 0.55f
}
