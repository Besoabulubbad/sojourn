package com.abulubad.sojourn.core.designsystem.components.staycard

/** How much room a [SojournStayCard] has. */
enum class SojournStayCardLayout {
    /** Lists: swipeable 16:10 photos on top, details below, no shadow. */
    Full,

    /** The map: one photo at the start, details beside it, on a raised `surface` card. */
    Compact,
}
