package com.abulubad.sojourn.core.designsystem.components.sheet

/** The three heights a [SojournSheet] can rest at (Sheet card). It stops only at these. */
enum class SojournSheetDetent {
    /** [SojournSheetDefaults.PeekHeight] above the bottom inset: the map's results handle. */
    Peek,

    /** Half the screen. */
    Half,

    /** Up to the status bar. */
    Full,
}
