package com.abulubad.sojourn.core.designsystem.components.iconbutton

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Values the IconButton card fixes that are not design tokens. */
object SojournIconButtonDefaults {
    /** The floating variant's visible disc; the touch target stays `touch-min`. */
    val FloatingDisc: Dp = 40.dp

    /** The count badge: at least 18 dp round, 4 dp in from the top-end corner. */
    val BadgeMinSize: Dp = 18.dp
    val BadgeInset: Dp = 4.dp
    val BadgePadding: Dp = 5.dp
}
