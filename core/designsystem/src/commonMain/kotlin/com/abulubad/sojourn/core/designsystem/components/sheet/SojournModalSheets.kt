package com.abulubad.sojourn.core.designsystem.components.sheet

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * How many modal [SojournSheet]s are open on screen. Our sheet draws in the page's own layer (ADR 0015), so unlike a
 * dialog window it does not hide the page from screen readers by itself: while [anyOpen], `SojournScaffold` removes
 * its page from the accessibility tree, and TalkBack and VoiceOver reach only the sheet. `SojournTheme` provides one.
 */
class SojournModalSheets {
    private var open by mutableIntStateOf(0)

    /** True while at least one modal sheet is shown. */
    val anyOpen: Boolean get() = open > 0

    internal fun opened() {
        open++
    }

    internal fun closed() {
        open--
    }
}

/** The screen's [SojournModalSheets]; a fresh one outside `SojournTheme` (previews of a single component). */
val LocalSojournModalSheets = staticCompositionLocalOf { SojournModalSheets() }
