package com.abulubad.sojourn.core.designsystem.components.sheet

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import com.abulubad.sojourn.core.designsystem.theme.settleSpring
import com.abulubad.sojourn.core.designsystem.tokens.SojournMotion

/**
 * Where a [SojournSheet] rests. [detents] are the stops it may use; it opens at [initial].
 * [current] is null while a modal sheet is hidden (before it enters and after it is dismissed).
 */
@Stable
class SojournSheetState internal constructor(
    val initial: SojournSheetDetent,
    val detents: Set<SojournSheetDetent>,
) {
    init {
        require(detents.isNotEmpty()) { "A sheet needs at least one detent." }
        require(initial in detents) { "The initial detent must be one of the detents." }
    }

    internal val anchored = AnchoredDraggableState(SheetStop.Hidden)

    val current: SojournSheetDetent?
        get() = anchored.currentValue.detent

    /** Moves the sheet to [detent], which must be one of [detents]. */
    suspend fun animateTo(detent: SojournSheetDetent) {
        require(detent in detents) { "$detent is not one of this sheet's detents." }
        anchored.animateTo(SheetStop.of(detent), SojournMotion.settleSpring())
    }

    /**
     * Animates a modal sheet out; its `onDismiss` then runs, as for every other way out. For an action that closes
     * the sheet, such as "Show 14 stays".
     */
    suspend fun hide() {
        anchored.animateTo(SheetStop.Hidden, exitSpec)
    }

    /** The exit motion, set by the sheet from the theme (reduced motion shortens it). */
    internal var exitSpec: AnimationSpec<Float> = SojournMotion.settleSpring()

    internal val lowest: SojournSheetDetent get() = SojournSheetDetent.entries.first { it in detents }
    internal val highest: SojournSheetDetent get() = SojournSheetDetent.entries.last { it in detents }
}

/** Remembers a [SojournSheetState]. A filters sheet uses the default; the map's results sheet peeks first. */
@Composable
fun rememberSojournSheetState(
    initial: SojournSheetDetent = SojournSheetDetent.Full,
    detents: Set<SojournSheetDetent> = setOf(initial),
): SojournSheetState = remember(initial, detents) { SojournSheetState(initial, detents) }

/** The anchors: the three detents plus the hidden position a modal sheet enters from and leaves to. */
internal enum class SheetStop(val detent: SojournSheetDetent?) {
    Hidden(null),
    Peek(SojournSheetDetent.Peek),
    Half(SojournSheetDetent.Half),
    Full(SojournSheetDetent.Full),
    ;

    companion object {
        fun of(detent: SojournSheetDetent): SheetStop = entries.first { it.detent == detent }
    }
}
